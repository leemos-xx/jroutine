package org.salp.jroutine.schedule.executor;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.salp.jroutine.AbstractLifecycle;
import org.salp.jroutine.Coroutine;
import org.salp.jroutine.exception.LifecycleException;
import org.salp.jroutine.schedule.NamedThreadFactory;
import org.salp.jroutine.schedule.WatchDog;

/**
 * implementation of Executor based on priority queue.
 * 
 * @author lihao
 * @date 2020-05-12
 */
public class PriorityExecutor extends AbstractLifecycle implements Executor<Coroutine> {

    private final static AtomicInteger idSource = new AtomicInteger(0);

    private PriorityBlockingQueue<Runnable> queue;
    private ThreadPoolExecutor threadPoolExecutor;
    private int id;
    private long lastSumittedTime = System.currentTimeMillis();

    private long keepAliveTime;
    private TimeUnit timeUnit;
    private int queueSize;

    private int weight;
    private int currentWeight;

    public PriorityExecutor(long keepAliveTime, TimeUnit timeUnit, int queueSize) {
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;

        this.id = idSource.incrementAndGet();
    }

    @Override
    protected void initInternal() throws LifecycleException {
        queue = new PriorityBlockingQueue<Runnable>(queueSize);
        threadPoolExecutor = new ThreadPoolExecutor(1, 1, keepAliveTime, timeUnit, queue,
                new NamedThreadFactory("EXECUTOR", false));

        WatchDog.me().addMonitor(new ExecutorMonitor(this));
    }

    @Override
    protected void startInternal() throws LifecycleException {

    }

    @Override
    protected void stopInternal() throws LifecycleException {
        threadPoolExecutor.shutdown();
    }

    @Override
    public void execute(Coroutine t) {
        threadPoolExecutor.execute(t);

        lastSumittedTime = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return "JROUTINE-EXECUTOR-E" + id;
    }

    public int getTaskSize() {
        return queue.size();
    }

    public long getIdleTime() {
        long idleTime = System.currentTimeMillis() - lastSumittedTime;
        return idleTime;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public int getCurrentWeight() {
        return currentWeight;
    }

    @Override
    public void setCurrentWeight(int weight) {
        currentWeight = weight;
    }
}
