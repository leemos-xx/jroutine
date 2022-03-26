package leemos.jroutine;

import leemos.jroutine.weave.OperandStack;

/**
 * 协程状态上下文
 */
public class CoroutineContext extends OperandStack {

    private static final long serialVersionUID = 2957061267209401968L;

    private static ThreadLocal<CoroutineContext> threadMap = new ThreadLocal<CoroutineContext>();

    // 是否需要恢复协程的上下文
    public volatile boolean restoring = false;
    // 是否需要暂存协程的上下文
    public volatile boolean capturing = false;
    // 处理完毕
    public volatile boolean done = false;

    /**
     * 获取协程上下文
     * @return
     */
    public static CoroutineContext get() {
        return threadMap.get();
    }

    /**
     * 设置协程上下文
     * @param recorder
     */
    public static void set(CoroutineContext recorder) {
        if (recorder == null) {
            throw new IllegalArgumentException();
        }
        threadMap.set(recorder);
    }

    /**
     * 清理上下文
     */
    public static void clear() {
        threadMap.remove();
    }

    /**
     * 标识当前上下文为挂起状态
     */
    public synchronized void suspend() {
        capturing = true;
        restoring = false;
    }

    /**
     * 标识当前上下文为恢复状态
     */
    public synchronized void resume() {
        capturing = false;
        restoring = true;
    }

    /**
     * 标识当前上下文为已经恢复完成
     */
    public synchronized void restored() {
        restoring = false;
    }

    /**
     * 标识当前协程已完成
     */
    public void done() {
        done = true;
    }

}
