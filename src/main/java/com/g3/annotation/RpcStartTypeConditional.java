package com.g3.annotation;

import com.g3.support.RpcStartTypeCondition;
import org.springframework.context.annotation.Conditional;

/**
 * @author : yesheng
 * @Description : 属性条件
 * @Date : 2018/12/4
 */
@Conditional(RpcStartTypeCondition.class)
public @interface RpcStartTypeConditional {

    /**
     * 内容
     * @return
     */
    String value();
}
