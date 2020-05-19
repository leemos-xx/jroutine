package org.salp.jroutine.observer;

/**
 * Observer
 * 
 * @author lihao
 * @date 2020-05-05
 */
public interface Observer<A> {

    void update(A action);
}
