package com.g3;

import org.springframework.context.ApplicationEvent;

/**
 * @author : yesheng
 * @Description : 监听事件，定时去监听服务器配置的变化
 * @Date : 2018/7/31
 */
public class HeartbeatEvent extends ApplicationEvent {

    private final Object state;

    /**
     * Create a new event with a source (for example a discovery client) and a value.
     * Neither parameter should be relied on to have specific content or format.
     *
     * @param source the source of the event
     * @param state the value indicating state of the catalog
     */
    public HeartbeatEvent(Object source, Object state) {
        super(source);
        this.state = state;
    }

    /**
     * A value representing the state of the service catalog. The only requirement is that
     * it changes when the catalog is updated, so it can be as simple as a version
     * conuter, or a hash. Implementations can provide information to help users visualize
     * what is going on in the catalog, but users should not rely on the content (since
     * the implementation of the underlying discovery might change).
     *
     * @return A value representing state of the service catalog
     */
    public Object getValue() {
        return this.state;
    }

}
