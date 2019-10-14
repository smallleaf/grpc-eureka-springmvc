package com.g3;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.discovery.CommonConstants;
import com.netflix.discovery.internal.util.Archaius1Utils;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/12/5
 */
public class RpcInstanceConfig {

    private static RpcInstanceConfig instance = new RpcInstanceConfig();

    private static DynamicPropertyFactory configInstance;

    static{
        configInstance = Archaius1Utils.initConfig(CommonConstants.CONFIG_FILE_NAME);
    }
    private RpcInstanceConfig(){

    }

    public  static RpcInstanceConfig getInstance(){
        return instance;
    }

    public String getRpcRegisterType(){
        return configInstance.getStringProperty(GrpcProperties.rpcRegisterType,GrpcProperties.RegisterType.EUREKA).getValue();
    }

    public String getRpcLocalServer(){
        return configInstance.getStringProperty(GrpcProperties.rpcServerLocal,null).get();
    }

    public String getServiceName(){
        return configInstance.getStringProperty(GrpcProperties.serviceName,null).get();
    }

}
