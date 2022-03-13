package org.salp.jroutine.schedule.lb;

/**
 * 负载均衡器
 */
public interface LoadBalancer {

    <T extends Instance> T select(T[] instances);
}
