package org.coral.jroutine.schedule;

import org.coral.jroutine.Lifecycle;
import org.coral.jroutine.schedule.lb.Instance;

/**
 * executor
 * 
 * @author lihao
 * @date 2020-05-12
 */
public interface Executor<T extends Runnable> extends Instance, Lifecycle {

    void execute(T t);

}
