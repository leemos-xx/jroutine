package org.salp.jroutine.schedule.executor;

import org.salp.jroutine.Lifecycle;
import org.salp.jroutine.schedule.lb.Instance;

/**
 * executor
 * 
 * @author lihao
 * @date 2020-05-12
 */
public interface Executor<T extends Runnable> extends Instance, Lifecycle {

    void execute(T t);

}
