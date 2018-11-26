package com.g3.config;

import com.g3.DiscoveryClientChannelFactory;
import com.g3.GrpcClientBeanPostProcessor;
import com.g3.InstanceInfoFactory;
import com.g3.SpringEurekaClient;
import com.netflix.appinfo.*;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/7/31
 */
public class EurekaConfiguration {
    @Bean
    @Order(1)
    public EurekaClient eurekaClient(ApplicationContext applicationContext){
        EurekaClientConfig eurekaClientConfig = new DefaultEurekaClientConfig();
        EurekaInstanceConfig eurekaInstanceConfig = new MyDataCenterInstanceConfig();

        ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(eurekaInstanceConfig, new InstanceInfoFactory().create(eurekaInstanceConfig));
        EurekaClient eurekaClient = new SpringEurekaClient(applicationInfoManager, eurekaClientConfig,applicationContext);
        eurekaClient.registerHealthCheck(new HealthCheckHandler() {
            @Override
            public InstanceInfo.InstanceStatus getStatus(InstanceInfo.InstanceStatus instanceStatus) {
                return InstanceInfo.InstanceStatus.UP;
            }
        });
        return eurekaClient;
    }


    @Bean
    public DiscoveryClientChannelFactory channelFactory(EurekaClient eurekaClient){
        return new DiscoveryClientChannelFactory(eurekaClient);
    }

    @Bean
    public GrpcClientBeanPostProcessor grpcClientBeanPostProcessor(){
        return new GrpcClientBeanPostProcessor();
    }
}
