package org.salp.jroutine.schedule.lb;

/**
 * 基于轮询的负载均衡
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private final PositiveAtomicInteger counter = new PositiveAtomicInteger();

    @Override
    public <T extends Instance> T select(T[] instances) {
        int length = instances.length;
        if (length == 0) {
            throw new IllegalArgumentException();
        }

        return instances[counter.getAndIncrement() % length];
    }

}
