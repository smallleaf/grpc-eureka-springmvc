package com.g3;

import io.grpc.ClientInterceptor;

import java.lang.annotation.*;

/**
 * @author : yesheng
 * @Description : GrpcClient使用该注解可以根据服务名去取对应的grpc配置
 * @see GrpcClientBeanPostProcessor
 * @Date : 2018/6/5
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GrpcClient {

    /**
     * 服务名称
     * @return
     */
    String value();

    /**
     * 全局拦截器
     * @return
     */
    Class<? extends ClientInterceptor>[] interceptors() default {};
}
