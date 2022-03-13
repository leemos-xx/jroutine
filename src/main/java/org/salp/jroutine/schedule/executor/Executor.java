package org.salp.jroutine.schedule.executor;

import org.salp.jroutine.Lifecycle;
import org.salp.jroutine.schedule.lb.Instance;

/**
 * 协程的Executor，实现Instance接口，可以通过LoadBalancer进行选择
 */
public interface Executor<T extends Runnable> extends Instance, Lifecycle {


    /**
     * 执行协程
     * @param t
     */
    void execute(T t);

    /**
     * 获取executor名
     * @return
     */
    String getName();

    /**
     * 获取当前executor协程数
     * @return
     */
    int getCoroutineSize();

    /**
     * 获取空闲时间
     * @return
     */
    long getIdleTime();
}
