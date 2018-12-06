package com.g3.support;

import com.g3.annotation.EurekaAutoConfiguration;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/12/5
 */
public class EurekaDeferredImportSelector implements DeferredImportSelector,BeanClassLoaderAware {



    private ClassLoader beanClassLoader;

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
                getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader());
        Assert.notEmpty(configurations,
                "No eureka auto configuration classes found in META-INF/spring.factories. If you "
                        + "are using a custom packaging, make sure that file is correct.");
        return configurations.toArray(new String[configurations.size()]);
    }


    protected ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }


    /**
     * Return the class used by {@link SpringFactoriesLoader} to load configuration
     * candidates.
     * @return the factory class
     */
    protected Class<?> getSpringFactoriesLoaderFactoryClass() {
        return EurekaAutoConfiguration.class;
    }


    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }
}
