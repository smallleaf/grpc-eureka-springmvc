package com.g3.config;

import com.g3.GrpcChannelFactory;
import com.g3.GrpcProperties;
import com.g3.RpcInstanceConfig;
import com.g3.annotation.RpcStartTypeConditional;
import com.g3.eureka.EurekaClientChannelFactory;
import com.g3.eureka.InstanceInfoFactory;
import com.g3.eureka.SpringEurekaClient;
import com.g3.local.LocalClientChannelFactory;
import com.g3.support.GrpcClientBeanPostProcessor;
import com.netflix.appinfo.*;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : yesheng
 * @Description : eureka客户端配置
 * @Date : 2018/7/31
 */
public class EurekaConfiguration  {

    @Configuration
    @RpcStartTypeConditional(GrpcProperties.RegisterType.EUREKA)
    public class EurekaConfig{
        @Bean
        public EurekaClient eurekaClient(ApplicationContext applicationContext){
            EurekaInstanceConfig eurekaInstanceConfig = new MyDataCenterInstanceConfig();
            EurekaClientConfig eurekaClientConfig = new DefaultEurekaClientConfig();
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
        public GrpcChannelFactory channelFactory(EurekaClient eurekaClient){
            return new EurekaClientChannelFactory(eurekaClient);
        }

    }

    @Configuration
    @RpcStartTypeConditional(GrpcProperties.RegisterType.LOCAL)
    public class LocalConfig{
        @Bean
        public GrpcChannelFactory channelFactory(){
            return new LocalClientChannelFactory(RpcInstanceConfig.getInstance().getRpcLocalServer());
        }
    }


    @Bean
    public GrpcClientBeanPostProcessor grpcClientBeanPostProcessor(){
        return new GrpcClientBeanPostProcessor();
    }


}
