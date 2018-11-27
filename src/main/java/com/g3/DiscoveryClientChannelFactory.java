package com.g3;

import com.g3.eureka.EurekaNameResolver;
import com.g3.eureka.EurekaNameResolverProvider;
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

import java.util.List;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/6/5
 */
public class DiscoveryClientChannelFactory implements GrpcChannelFactory {

    private Logger log = LoggerFactory.getLogger(DiscoveryClientChannelFactory.class);

    private static final String EUREKA = "eureka://";
    /**
     * grpc的端口命名
     */
    private static final String GRPC_PORT = "grpc.server.port";

    private EurekaClient discoveryClient;

    private HeartbeatMonitor monitor = new HeartbeatMonitor();

    private List<EurekaNameResolver> discoveryClientNameResolvers = Lists.newArrayList();

    public DiscoveryClientChannelFactory(EurekaClient discoveryClient){
        this.discoveryClient = discoveryClient;
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
}
