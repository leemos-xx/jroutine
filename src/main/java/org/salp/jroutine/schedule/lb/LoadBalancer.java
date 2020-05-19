package org.salp.jroutine.schedule.lb;

/**
 * load balancer.
 * 
 * @author lihao
 * @date 2020-05-12
 */
public interface LoadBalancer {

    public <T extends Instance> T select(T[] instances);
}
