package com.g3.eureka;

import com.g3.GrpcChannelFactory;
import com.g3.HeartbeatEvent;
import com.g3.HeartbeatMonitor;
import com.g3.RpcInstanceConfig;
import com.google.common.collect.Lists;
import com.netflix.discovery.EurekaClient;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/6/5
 */
public class EurekaClientChannelFactory implements GrpcChannelFactory {

    private Logger log = LoggerFactory.getLogger(EurekaClientChannelFactory.class);

    private static final String EUREKA = "eureka://";
    /**
     * grpc的端口命名
     */
    private static final String GRPC_PORT = "grpc.server.port";

    private EurekaClient discoveryClient;

    private HeartbeatMonitor monitor = new HeartbeatMonitor();

    private List<EurekaNameResolver> discoveryClientNameResolvers = Lists.newArrayList();

    /**
     * 默认的grpc地址
     */
    private InetSocketAddress defaultAddress;


    public EurekaClientChannelFactory(EurekaClient discoveryClient){
        this.discoveryClient = discoveryClient;
        String localServer = RpcInstanceConfig.getInstance().getRpcLocalServer();
        if(localServer != null){
            try {
                String[] address = localServer.split(":");
                defaultAddress  = new InetSocketAddress(address[0],Integer.valueOf(address[1]));
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }

    }


    public void addEurekaNameResolverProvider(EurekaNameResolver eurekaNameResolver){
        discoveryClientNameResolvers.add(eurekaNameResolver);
    }

    @EventListener(HeartbeatEvent.class)
    public void heartbeat(HeartbeatEvent event){
        if(this.monitor.update(event.getValue())){
            log.debug("refesh.....");
            for(EurekaNameResolver eurekaNameResolver : discoveryClientNameResolvers){
                eurekaNameResolver.refresh();
            }
        }
    }

    @Override
    public Channel createChannel(String name, List<ClientInterceptor> interceptors) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(EUREKA+name)
                .nameResolverFactory(new EurekaNameResolverProvider(discoveryClient,GRPC_PORT,this))
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .usePlaintext(true)
                .build();
        return channel;
    }

    public InetSocketAddress getDefaultAddress() {
        return defaultAddress;
    }
}
