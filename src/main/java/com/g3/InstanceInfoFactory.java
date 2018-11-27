package com.g3;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author : yesheng
 * @Description : 从配置文件中获取配置信息
 * @Date : 2018/8/1
 */
public class InstanceInfoFactory {

    private Logger log = LoggerFactory.getLogger(InstanceInfoFactory.class);

    public InstanceInfo create(EurekaInstanceConfig config) {
        LeaseInfo.Builder leaseInfoBuilder = LeaseInfo.Builder.newBuilder()
                .setRenewalIntervalInSecs(config.getLeaseRenewalIntervalInSeconds())
                .setDurationInSecs(config.getLeaseExpirationDurationInSeconds());

        // Builder the instance information to be registered with eureka
        // server
        InstanceInfo.Builder builder = InstanceInfo.Builder.newBuilder();

        String namespace = config.getNamespace();
        if (!namespace.endsWith(".")) {
            namespace = namespace + ".";
        }
        builder.setNamespace(namespace).setAppName(config.getAppname())
                .setInstanceId(config.getInstanceId())
                .setAppGroupName(config.getAppGroupName())
                .setDataCenterInfo(config.getDataCenterInfo())
                .setIPAddr(config.getIpAddress()).setHostName(config.getHostName(false))
                .setPort(config.getNonSecurePort())
                .enablePort(InstanceInfo.PortType.UNSECURE,
                        config.isNonSecurePortEnabled())
                .setSecurePort(config.getSecurePort())
                .enablePort(InstanceInfo.PortType.SECURE, config.getSecurePortEnabled())
                .setVIPAddress(config.getVirtualHostName())
                .setSecureVIPAddress(config.getSecureVirtualHostName())
                .setHomePageUrl(config.getHomePageUrlPath(), config.getHomePageUrl())
                .setStatusPageUrl(config.getStatusPageUrlPath(),
                        config.getStatusPageUrl())
                .setHealthCheckUrls(config.getHealthCheckUrlPath(),
                        config.getHealthCheckUrl(), config.getSecureHealthCheckUrl())
                .setASGName(config.getASGName());

        // Start off with the STARTING state to avoid traffic
        if (!config.isInstanceEnabledOnit()) {
            InstanceInfo.InstanceStatus initialStatus = InstanceInfo.InstanceStatus.STARTING;
            if (log.isInfoEnabled()) {
                log.info("Setting initial instance status as: " + initialStatus);
            }
            builder.setStatus(initialStatus);
        }
        else {
            if (log.isInfoEnabled()) {
                log.info("Setting initial instance status as: "
                        + InstanceInfo.InstanceStatus.UP
                        + ". This may be too early for the instance to advertise itself as available. "
                        + "You would instead want to control this via a healthcheck handler.");
            }
        }

        // Add any user-specific metadata information
        for (Map.Entry<String, String> mapEntry : config.getMetadataMap().entrySet()) {
            String key = mapEntry.getKey();
            String value = mapEntry.getValue();
            builder.add(key, value);
        }

        InstanceInfo instanceInfo = builder.build();
        instanceInfo.setLeaseInfo(leaseInfoBuilder.build());
        return instanceInfo;
    }
}
