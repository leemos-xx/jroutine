package org.salp.jroutine;

import java.util.concurrent.atomic.AtomicInteger;

import org.salp.jroutine.exception.IllegalTaskStateException;
import org.salp.jroutine.exception.NonEnhancedClassException;
import org.salp.jroutine.observer.Observable;
import org.salp.jroutine.weave.OperandStackRecoder;

/**
 * 协程，可以看做是轻量级的线程，但是它的调度策略是在应用层实现的，其上下文切换的开销会小于系统级线程的上下文切换开销。
 * 在Jroutine实现的协程调度策略中，支持协程的挂起、恢复和结束。
 */
public class Coroutine extends Observable<CoroutineState> implements Runnable, Comparable<Coroutine> {

    // 用于生成Coroutine的id，默认单调递增
    private final static AtomicInteger idSource = new AtomicInteger(0);
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
    private OperandStackRecoder recorder;

    public Coroutine(Runnable target) {
        this(PREFIX_NAME, target, Constants.DEFAULT_PRIORITY);
    }

    public Coroutine(String name, Runnable target) {
        this(name, target, Constants.DEFAULT_PRIORITY);
    }

    public Coroutine(Runnable target, int priority) {
        this(PREFIX_NAME, target, priority);
    }

    public Coroutine(String name, Runnable target, int priority) {
        this.target = target;
        this.recorder = new OperandStackRecoder(target);
        this.id = idSource.getAndIncrement();

        this.setName(name);
        this.setPriority(priority);
    }

    public final void run() {
        if (status != CoroutineState.NEW) {
            throw new IllegalTaskStateException();
        }

        if (!(target instanceof Enhanced)) {
            throw new NonEnhancedClassException();
        }

        try {
            setStatus(CoroutineState.RUNNABLE);

            OperandStackRecoder.set(this.recorder);

            target.run();

            // TODO how to judge whether the task has been completed

        } catch (Exception e) {
            setStatus(CoroutineState.TERMINATED);
            throw e;
        } finally {
            // OperandStackRecoder.clear();
        }
    }

    public synchronized void suspend() {
        if (status != CoroutineState.RUNNABLE) {
            throw new IllegalTaskStateException();
        }
        setStatus(CoroutineState.SUSPENDING);

        recorder.suspend();
    }

    public void resume() {
        if (status != CoroutineState.SUSPENDING) {
            throw new IllegalTaskStateException();
        }
        setStatus(CoroutineState.RUNNABLE);

        target.run();
    }

    public void stop() {
        if (status == CoroutineState.NEW) {
            throw new IllegalTaskStateException();
        }
        setStatus(CoroutineState.TERMINATED);

        recorder.suspend();
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (status != CoroutineState.NEW) {
            throw new IllegalTaskStateException();
        }
        this.name = name;
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

    protected void setStatus(CoroutineState status) {
        if (this.status == CoroutineState.TERMINATED) {
            throw new IllegalTaskStateException();
        }
        this.status = status;

        notifyObservers(status);
    }

}
