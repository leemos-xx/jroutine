package leemos.jroutine.schedule;

/**
 * 调度器
 * @param <T>
 */
public interface Scheduler<T extends Runnable> {

    /**
     * 提交任务
     * @param t
     */
    void submit(T t);

    /**
     * 挂起任务
     * @param t
     */
    void suspend(T t);

    /**
     * 恢复任务
     * @param t
     */
    void resume(T t);
}
