package org.coral.jroutine.schedule.lb;

/**
 * smooth weighted round robin for load balance.
 * 
 * @author lihao
 * @date 2020-05-15
 */
public class WeightRoundRobinLoadBalancer implements LoadBalancer {

    @Override
    public <T extends Instance> T select(T[] instances) {
        int length = instances.length;
        if (length == 0) {
            throw new IllegalArgumentException();
        }

        T maxWeightInstance = getMaxWeightInstance(instances);

        recountWeight(instances);

        return maxWeightInstance;
    }

    private <T extends Instance> T getMaxWeightInstance(T[] instances) {
        T maxWeightInstance = instances[0];
        int weightSum = 0;

        for (T instance : instances) {
            weightSum += instance.getWeight();

            if (instance.getCurrentWeight() > maxWeightInstance.getCurrentWeight()) {
                maxWeightInstance = instance;
            }
        }

        maxWeightInstance.setCurrentWeight(maxWeightInstance.getCurrentWeight() - weightSum);

        return maxWeightInstance;
    }

    private <T extends Instance> void recountWeight(T[] instances) {
        for (T instance : instances) {
            instance.setCurrentWeight(instance.getCurrentWeight() + instance.getWeight());
        }
    }

}
