package org.salp.jroutine;

import org.salp.jroutine.exception.LifecycleException;

/**
 * simply define the events and status in the lifecycle.
 * 
 * @author lihao
 * @date 2020-05-12
 */
public interface Lifecycle {

    public void init() throws LifecycleException;

    public void start() throws LifecycleException;

    public void stop() throws LifecycleException;

    public enum State {
        NEW, INITIALIZED, STARTED, STOPPED;
    }
}
