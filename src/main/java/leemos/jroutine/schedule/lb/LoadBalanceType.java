package leemos.jroutine.schedule.lb;

/**
 * 负载均衡器的类型
 */
public enum LoadBalanceType {
    // 轮询
    ROUND_ROBIN,
    // 加权轮询
    WEIGHT_ROUND_ROBIN;
}
