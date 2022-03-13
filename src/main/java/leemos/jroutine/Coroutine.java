package leemos.jroutine;

import java.util.concurrent.atomic.AtomicInteger;

import leemos.jroutine.exception.IllegalCoroutineStateException;
import leemos.jroutine.exception.NonEnhancedClassException;

/**
 * 协程，可以看做是轻量级的线程，但是它的调度策略是在应用层实现的，其上下文切换的开销会小于系统级线程的上下文切换开销。
 * 在Jroutine实现的协程调度策略中，支持协程的挂起、恢复和结束。
 */
public class Coroutine extends Observable<CoroutineState> implements Runnable, Comparable<Coroutine> {

    // 用于生成Coroutine的id，默认单调递增
    private final static AtomicInteger ID_SOURCE = new AtomicInteger(0);
    private final static String PREFIX_NAME = "COROUTINE-";

    // 协程状态
    private volatile CoroutineState status = CoroutineState.NEW;

    // 协程的唯一id
    private int id;

    // 协程名
    private String name;

    // 协程的优先级，影响调度策略
    private int priority;

    // target需要实现Runnable接口，其run方法中为实际的待执行的业务。
    // 此处的target需要先进行字节码增强，以保证其在运行过程中可以被中断，
    // 该中断并非系统线程级的时钟中断，而是在应用层面中断协程，以保证应用层能对协程进行主动调度。
    private Runnable target;

    // 当前协程运行状态的上下文
    private CoroutineContext context;

    public Coroutine(Runnable target) {
        this(target, Constants.DEFAULT_PRIORITY);
    }

    public Coroutine(Runnable target, int priority) {
        this.id = ID_SOURCE.getAndIncrement();
        this.name = PREFIX_NAME + this.id;
        this.target = target;
        this.priority = priority;

        this.context = new CoroutineContext(target);
    }

    @Override
    public void run() {
        // 检查Coroutine的状态，是否可执行
        if (status != CoroutineState.NEW) {
            throw new IllegalCoroutineStateException();
        }
        // target必须先经过字节码增强才能运行
        if (!(target instanceof Enhanced)) {
            throw new NonEnhancedClassException();
        }

        setStatus(CoroutineState.RUNNABLE);
        try {
            CoroutineContext.set(this.context);
            target.run();
        } catch (Exception e) {
            setStatus(CoroutineState.TERMINATED);
            throw e;
        } finally {
            CoroutineContext.clear();
        }
    }

    /**
     * 挂起协程
     */
    public synchronized void suspend() {
        if (status != CoroutineState.RUNNABLE) {
            throw new IllegalCoroutineStateException();
        }
        setStatus(CoroutineState.SUSPENDING);

        context.suspend();
    }

    /**
     * 恢复协程
     */
    public synchronized void resume() {
        if (status != CoroutineState.SUSPENDING) {
            throw new IllegalCoroutineStateException();
        }
        setStatus(CoroutineState.RUNNABLE);

        // FIXME 此处需要根据上次suspend的上下文继续执行
        target.run();
    }

    /**
     * 停止协程
     */
    public synchronized void stop() {
        if (status == CoroutineState.NEW) {
            throw new IllegalCoroutineStateException();
        }
        setStatus(CoroutineState.TERMINATED);

        // FIXME 此处除挂起协程外，应该保证其不再被调度
        context.suspend();
    }

    public String getName() {
        return name;
    }

    public void setPriority(int priority) {
        if (priority > Constants.MAX_PRIORITY || priority < Constants.MIN_PRIORITY) {
            throw new IllegalArgumentException();
        }
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(Coroutine t) {
        return this.priority >= t.priority ? 1 : -1;
    }

    /**
     * 修改协程状态，并通知所有观察者
     * @param status
     */
    private void setStatus(CoroutineState status) {
        if (this.status == CoroutineState.TERMINATED) {
            throw new IllegalCoroutineStateException();
        }
        this.status = status;

        notifyObservers(status);
    }

}
