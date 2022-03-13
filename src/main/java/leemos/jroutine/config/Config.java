package leemos.jroutine.config;

import leemos.jroutine.schedule.lb.LoadBalanceType;

import java.util.concurrent.TimeUnit;

/**
 * 配置文件
 * TODO 该类应该从配置文件中读取生成
 */
public class Config {

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
