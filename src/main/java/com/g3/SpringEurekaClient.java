package com.g3;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/7/31
 */
public class SpringEurekaClient extends DiscoveryClient {

    private static final Logger log = LoggerFactory.getLogger(SpringEurekaClient.class);

    private final AtomicLong cacheRefreshedCount = new AtomicLong(0);

    private ApplicationContext publisher;

    public SpringEurekaClient(ApplicationInfoManager applicationInfoManager,
                             EurekaClientConfig config,
                              ApplicationContext publisher) {
        super(applicationInfoManager, config);
        this.publisher = publisher;
    }

    @Override
    protected void onCacheRefreshed() {
        if (this.cacheRefreshedCount != null) { //might be called during construction and will be null
            long newCount = this.cacheRefreshedCount.incrementAndGet();
            log.trace("onCacheRefreshed called with count: " + newCount);
            this.publisher.publishEvent(new HeartbeatEvent(this, newCount));
        }
    }
}
