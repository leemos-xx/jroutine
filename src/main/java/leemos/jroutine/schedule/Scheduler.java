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
}
