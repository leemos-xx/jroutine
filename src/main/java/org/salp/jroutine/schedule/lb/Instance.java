package org.salp.jroutine.schedule.lb;


/**
 * 待负载均衡处理的逻辑单元
 */
public interface Instance {

    /**
     * 获取基础权重
     * @return
     */
    int getWeight();

    /**
     * 获取当前权重
     * @return
     */
    int getCurrentWeight();

    /**
     * 设置当前权重
     * @param weight
     */
    void setCurrentWeight(int weight);
}
