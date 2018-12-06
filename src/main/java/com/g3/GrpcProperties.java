package com.g3;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/12/5
 */
public class GrpcProperties {

    public static class RegisterType{

        public static final String LOCAL = "local";

        public static final String EUREKA = "eureka";

        public static final String Zookeeper = "zookeeper";
    }

    public static final String rpcServerLocal = "rpc.server.local";

    public static final String rpcRegisterType = "rpc.register.type";


}
