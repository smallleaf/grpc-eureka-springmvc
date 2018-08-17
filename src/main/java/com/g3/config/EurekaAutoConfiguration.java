package com.g3.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/7/31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EurekaConfiguration.class)
public @interface EurekaAutoConfiguration {

}
