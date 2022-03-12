package org.salp.jroutine;

import org.salp.jroutine.exception.LifecycleException;

/**
 * 生命周期接口定义
 */
public interface Lifecycle {

    /**
     * 初始化
     * @throws LifecycleException
     */
    void init() throws LifecycleException;

    /**
     * 启动
     * @throws LifecycleException
     */
    void start() throws LifecycleException;

    /**
     * 结束
     * @throws LifecycleException
     */
    void stop() throws LifecycleException;

    /**
     * 定义生命周期状态
     */
    enum State {
        // 新创建，未经过任何操作
        NEW,
        // 已初始化
        INITIALIZED,
        // 已启动
        STARTED,
        // 已结束
        STOPPED;
    }
}
