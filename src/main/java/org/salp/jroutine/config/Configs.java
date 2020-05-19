package org.salp.jroutine.config;

import java.util.concurrent.TimeUnit;

/**
 * global configs
 * 
 * @author lihao
 * @date 2020-05-06
 */
public class Configs {

    private static boolean debug = true;
    private static int executorsCoreSize = -1;
    private static ExtensionType extensionType = ExtensionType.METHOD;
    private static LoadBalanceType loadBalanceType = null;
    private static long threadKeepAliveTime = -1;
    private static TimeUnit keepAliveTimeUnit = null;
    private static int executorQueueSize = -1;

    public static boolean isDebugEnabled() {
        return debug;
    }

    public static int getExecutorsCoreSize() {
        return executorsCoreSize;
    }

    public static ExtensionType getExtensionType() {
        return extensionType;
    }

    public static LoadBalanceType getLoadBalanceType() {
        return loadBalanceType;
    }

    public static long getThreadKeepAliveTime() {
        return threadKeepAliveTime;
    }

    public static TimeUnit getKeepAliveTimeUnit() {
        return keepAliveTimeUnit;
    }

    public static int getExecutorQueueSize() {
        return executorQueueSize;
    }

}
