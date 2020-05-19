package org.salp.jroutine.weave;

/**
 * record the status of the operand stack¡£
 * 
 * @author lihao
 * @date 2020-05-10
 */
public class OperandStackRecoder extends OperandStack {

    private static final long serialVersionUID = 2957061267209401968L;

    private static ThreadLocal<OperandStackRecoder> threadMap = new ThreadLocal<OperandStackRecoder>();

    public volatile boolean isRestoring = false;
    public volatile boolean isCapturing = false;
    public volatile boolean isDone = false;

    public OperandStackRecoder(Runnable pRunnable) {
        super(pRunnable);
    }

    public static OperandStackRecoder get() {
        return threadMap.get();
    }

    public static void set(OperandStackRecoder recorder) {
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
