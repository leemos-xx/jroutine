package org.salp.jroutine.schedule;

/**
 * the interface of scheduler.
 * 
 * @author lihao
 * @date 2020-05-12
 */
public interface Scheduler<T extends Runnable> {

    public void submit(T t);
}
