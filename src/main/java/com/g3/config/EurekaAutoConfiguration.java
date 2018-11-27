package com.g3.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author : yesheng
 * @Description : 注册开启客户端从注册中心配置
 * @Date : 2018/7/31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EurekaConfiguration.class)
public @interface EurekaAutoConfiguration {

}
