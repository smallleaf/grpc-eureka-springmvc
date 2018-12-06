package com.g3.support;

import com.g3.GrpcProperties;
import com.g3.RpcInstanceConfig;
import com.g3.annotation.RpcStartTypeConditional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/12/4
 */
public class RpcStartTypeCondition implements Condition {

    private Logger logger = LoggerFactory.getLogger(RpcStartTypeCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> properteisConditional =  metadata.getAnnotationAttributes(RpcStartTypeConditional.class.getName());
        if(properteisConditional == null){
            logger.debug("the RpcStartTypeCondition must apply with RpcStartTypeConditional ");
            throw  new RuntimeException("the RpcStartTypeCondition must apply with RpcStartTypeConditional ");
        }
        RpcInstanceConfig rpcInstanceConfig = RpcInstanceConfig.getInstance();
        String rpcType = rpcInstanceConfig.getRpcRegisterType();
        if(rpcType == null){
            rpcType = GrpcProperties.RegisterType.EUREKA;
        }
        String value = String.valueOf(properteisConditional.get("value"));
        if(rpcType.equalsIgnoreCase(value)){
            return true;
        }
        return false;
    }
}
