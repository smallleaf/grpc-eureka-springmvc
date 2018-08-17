package com.g3;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/7/31
 */
public class HeartbeatMonitor {

    private AtomicReference<Object> latestHeartbeat = new AtomicReference<>();

    /**
     * @param value the latest heartbeat
     * @return true if the state changed
     */
    public boolean update(Object value) {
        Object last = this.latestHeartbeat.get();
        if (value != null && !value.equals(last)) {
            return this.latestHeartbeat.compareAndSet(last, value);
        }
        return false;
    }

}
