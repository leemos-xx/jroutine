package org.coral.jroutine;

import org.coral.jroutine.exception.LifecycleException;

/**
 * events need to be executed in order.
 * 
 * @author lihao
 * @date 2020-05-12
 */
public abstract class AbstractLifecycle implements Lifecycle {

    private State status = State.NEW;

    @Override
    public void init() throws LifecycleException {
        if (status != State.NEW) {
            throw new LifecycleException();
        }
        try {
            initInternal();
            setStatus(State.INITIALIZED);
        } catch (RuntimeException e) {
            throw new LifecycleException(e);
        }
    }

    protected abstract void initInternal() throws LifecycleException;

    @Override
    public void start() throws LifecycleException {
        if (status == State.NEW) {
            init();
        }
        if (status != State.INITIALIZED) {
            throw new LifecycleException();
        }

        try {
            startInternal();
            setStatus(State.STARTED);
        } catch (RuntimeException e) {
            throw new LifecycleException(e);
        }
    }

    protected abstract void startInternal() throws LifecycleException;

    @Override
    public void stop() throws LifecycleException {
        if (status != State.STARTED) {
            throw new LifecycleException();
        }

        try {
            stopInternal();
            setStatus(State.STOPPED);
        } catch (RuntimeException e) {
            throw new LifecycleException(e);
        }
    }

    protected abstract void stopInternal() throws LifecycleException;

    public void setStatus(State status) {
        this.status = status;
    }
}
