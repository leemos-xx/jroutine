package org.salp.jroutine.schedule.lb;

/**
 * round robin for load balance.
 * 
 * @author lihao
 * @date 2020-05-12
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
