package org.salp.jroutine.schedule.lb;

/**
 * 基于加权轮询的负载均衡
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

    /**
     * 获取权重最高的实例
     * @param instances
     * @param <T>
     * @return
     */
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

    /**
     * 重新计算各个实例的权重
     * @param instances
     * @param <T>
     */
    private <T extends Instance> void recountWeight(T[] instances) {
        for (T instance : instances) {
            instance.setCurrentWeight(instance.getCurrentWeight() + instance.getWeight());
        }
    }

}
