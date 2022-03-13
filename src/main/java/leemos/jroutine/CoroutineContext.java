package leemos.jroutine;

import leemos.jroutine.weave.OperandStack;

/**
 * 协程状态上下文
 */
public class CoroutineContext extends OperandStack {

    private static final long serialVersionUID = 2957061267209401968L;

    private static ThreadLocal<CoroutineContext> threadMap = new ThreadLocal<CoroutineContext>();

    public volatile boolean isRestoring = false;
    public volatile boolean isCapturing = false;
    public volatile boolean isDone = false;

    public CoroutineContext(Runnable pRunnable) {
        super(pRunnable);
    }

    public static CoroutineContext get() {
        return threadMap.get();
    }

    public static void set(CoroutineContext recorder) {
        if (recorder == null) {
            throw new IllegalArgumentException();
        }
        threadMap.set(recorder);
    }

    public static void clear() {
        threadMap.remove();
    }

    public void suspend() {
        isCapturing = !isRestoring;
        isRestoring = false;
    }

    public void done() {
        isDone = true;
    }

}
