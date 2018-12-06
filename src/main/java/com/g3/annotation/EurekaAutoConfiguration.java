package com.g3.annotation;

import com.g3.support.EurekaDeferredImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author : yesheng
 * @Description : 注册开启客户端从注册中心配置
 * @see  EurekaDeferredImportSelector
 * @Date : 2018/7/31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EurekaDeferredImportSelector.class)
public @interface EurekaAutoConfiguration {

}
