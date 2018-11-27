package com.g3.eureka;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/6/4
 */
public class EurekaNameResolver extends NameResolver {

    private Logger log = LoggerFactory.getLogger(EurekaNameResolver.class);

    /**
     * 服务名称
     */
    private final String serviceName;

    /**
     * grpc端口的配置名称
     */
    private final String portMetaData;

    /**
     *
     */
    private final EurekaClient client;
    private final SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource;
    private final SharedResourceHolder.Resource<ExecutorService> executorResource;

    @GuardedBy("this")
    private boolean shutdown;

    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;

    @GuardedBy("this")
    private ScheduledExecutorService timerService;
    @GuardedBy("this")
    private ExecutorService executor;
    @GuardedBy("this")
    private ScheduledFuture<?> resolutionTask;

    @GuardedBy("this")
    private List<InstanceInfo> serviceInstanceList;


    public EurekaNameResolver(EurekaClient client, URI targetUri, String portMetaData) {
        this.portMetaData = portMetaData;
        serviceName = targetUri.getAuthority();
        this.client = client;
        this.timerServiceResource = GrpcUtil.TIMER_SERVICE;
        this.executorResource = GrpcUtil.SHARED_CHANNEL_EXECUTOR;
        this.serviceInstanceList = Lists.newArrayList();

    }

    @Override
    public String getServiceAuthority() {
        return serviceName;
    }

    @Override
    public void start(Listener listener) {
        Preconditions.checkState(this.listener == null, "already started");
        timerService = SharedResourceHolder.get(timerServiceResource);
        this.listener = listener;
        executor = SharedResourceHolder.get(executorResource);
        this.listener = Preconditions.checkNotNull(listener, "listener");
        resolve();
    }


    @Override
    public final synchronized void refresh() {
        if (listener != null) {
            resolve();
        }
    }

    @GuardedBy("this")
    private void resolve() {
        if (resolving || shutdown) {
            return;
        }
        executor.execute(resolutionRunnable);
    }
    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        if (resolutionTask != null) {
            resolutionTask.cancel(false);
        }
        if (timerService != null) {
            timerService = SharedResourceHolder.release(timerServiceResource, timerService);
        }
        if (executor != null) {
            executor = SharedResourceHolder.release(executorResource, executor);
        }
    }


    /**
     * 异步执行
     * 从eureka注册中的服务配置中，去取配置
     * 如果配置发生变化，则修改channnel的address
     */
    private final Runnable resolutionRunnable = new Runnable() {
        @Override
        public void run() {
            Listener savedListener;
            synchronized (EurekaNameResolver.this) {
                // If this task is started by refresh(), there might already be a scheduled task.
                if (resolutionTask != null) {
                    resolutionTask.cancel(false);
                    resolutionTask = null;
                }
                if (shutdown) {
                    return;
                }
                savedListener = listener;
                resolving = true;
            }
            try {
                List<InstanceInfo> newServiceInstanceList;
                try {
                    newServiceInstanceList = client.getApplication(serviceName).getInstances();
                } catch (Exception e) {
                    savedListener.onError(Status.UNAVAILABLE.withCause(e));
                    return;
                }

                if (newServiceInstanceList != null && newServiceInstanceList.size()>0) {
                    if (isNeedToUpdateServiceInstanceList(newServiceInstanceList)) {
                        serviceInstanceList = newServiceInstanceList;
                    } else {
                        return;
                    }
                    List<EquivalentAddressGroup> equivalentAddressGroups = Lists.newArrayList();
                    for (InstanceInfo serviceInstance : serviceInstanceList) {
                        Map<String, String> metadata = serviceInstance.getMetadata();
                        if (metadata.get(portMetaData) != null) {
                            Integer port = Integer.valueOf(metadata.get(portMetaData));
                            log.info("Found gRPC server {} {}:{}", serviceName, serviceInstance.getHostName(), port);
                            EquivalentAddressGroup addressGroup = new EquivalentAddressGroup(new InetSocketAddress
                                    (serviceInstance.getHostName(), port), Attributes.EMPTY);
                            equivalentAddressGroups.add(addressGroup);
                        } else {
                            log.error("Can not found gRPC server {}", serviceName);
                        }
                    }
                    savedListener.onAddresses(equivalentAddressGroups, Attributes.EMPTY);
                } else {
                    savedListener.onError(Status.UNAVAILABLE.withCause(new RuntimeException("UNAVAILABLE: NameResolver returned an empty list")));
                }
            } finally {
                synchronized (EurekaNameResolver.this) {
                    resolving = false;
                }
            }
        }
    };


    /**
     * 判断获取的配置是否发生改变，有两个地方需要判断，一个是hostname，一个port
     * @param newServiceInstanceList
     * @return
     */
    private boolean isNeedToUpdateServiceInstanceList(List<InstanceInfo> newServiceInstanceList) {
        if (serviceInstanceList.size() == newServiceInstanceList.size()) {
            for (InstanceInfo serviceInstance : serviceInstanceList) {
                boolean isSame = false;
                for (InstanceInfo newServiceInstance : newServiceInstanceList) {

                    //判断是否发生了改变  host  grpc的端口
                    if (newServiceInstance.getHostName().equals(serviceInstance.getHostName()) && newServiceInstance.getPort() ==
                            serviceInstance.getPort()) {
                        Map<String, String> newmetadata = newServiceInstance.getMetadata();
                        Map<String, String> metadata = serviceInstance.getMetadata();
                        if(metadata.get(portMetaData) != null && newmetadata.get(portMetaData) != null &&metadata.get
                                (portMetaData).equals(newmetadata.get(portMetaData))){
                            isSame = true;
                            break;
                        }

                    }
                }
                if (!isSame) {
                    log.info("Ready to update {} server info group list", serviceName);
                    return true;
                }
            }
        } else {
            log.info("Ready to update {} server info group list", serviceName);
            return true;
        }
        return false;
    }

}
