package org.salp.jroutine;

import org.salp.jroutine.exception.LifecycleException;

/**
 * 生命周期骨干实现
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
        // 若启动时未初始化，则先执行初始化方法
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

    private void setStatus(State status) {
        this.status = status;
    }
}
