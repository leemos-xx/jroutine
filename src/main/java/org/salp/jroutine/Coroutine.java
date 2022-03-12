package org.salp.jroutine;

import java.util.concurrent.atomic.AtomicInteger;

import org.salp.jroutine.exception.IllegalTaskStateException;
import org.salp.jroutine.exception.NonEnhancedClassException;
import org.salp.jroutine.observer.Observable;
import org.salp.jroutine.weave.OperandStackRecoder;

/**
 * Coroutine, which can be regarded as a lightweight thread, scheduled in the
 * application layer, supporting suspend, resume and stop.
 * 
 * @author lihao
 * @date 2020-04-29
 */
public class Coroutine extends Observable<CoroutineState> implements Runnable, Comparable<Coroutine> {

    private final static AtomicInteger idSource = new AtomicInteger(0);
    private final static String DEFAULT_TASK_PREFIX_NAME = "DEFAULT-TASK-";
    private final static int MIN_PRIORITY = 1;
    private final static int DEFAULT_PRIORITY = 5;
    private final static int MAX_PRIORITY = 10;

    private int id;
    private String name;
    private int priority;
    // enhanced class
    private Runnable target;
    // each task needs to hold an operand stack recorder, to record the execution
    // data of the current task.
    private OperandStackRecoder recorder;

    private volatile CoroutineState status = CoroutineState.NEW;

    public Coroutine(Runnable target) {
        this(DEFAULT_TASK_PREFIX_NAME, target, DEFAULT_PRIORITY);
    }

    public Coroutine(String name, Runnable target) {
        this(name, target, DEFAULT_PRIORITY);
    }

    public Coroutine(Runnable target, int priority) {
        this(DEFAULT_TASK_PREFIX_NAME, target, priority);
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
        if (priority > MAX_PRIORITY || priority < MIN_PRIORITY) {
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
