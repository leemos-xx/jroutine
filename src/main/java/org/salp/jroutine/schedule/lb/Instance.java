package org.salp.jroutine.schedule.lb;

import org.salp.jroutine.exception.NotImplementedException;

/**
 * unit of load balancer.
 * 
 * @author lihao
 * @date 2020-05-12
 */
public interface Instance {

    default int getWeight() {
        throw new NotImplementedException();
    }

    default int getCurrentWeight() {
        throw new NotImplementedException();
    }

    default void setCurrentWeight(int weight) {
        throw new NotImplementedException();
    }
}
