package leemos.jroutine.schedule.executor;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import leemos.jroutine.Coroutine;
import leemos.jroutine.AbstractLifecycle;
import leemos.jroutine.exception.LifecycleException;
import leemos.jroutine.schedule.NamedThreadFactory;

/**
 * Executor的实现类，具备优先级调度功能
 */
public class PriorityExecutor extends AbstractLifecycle implements Executor<Coroutine> {

    // 用于生成executor的id，默认单调递增
    private final static AtomicInteger ID_SOURCE = new AtomicInteger(0);
    private final static String PREFIX_NAME = "EXECUTOR-";

    // 优先级队列，优先级较高的协程将会优先执行
    private PriorityBlockingQueue<Runnable> queue;

    // 核心线程池
    private ThreadPoolExecutor coreThreadPool;

    // executor唯一标识
    private int id;
    // executor名称
    private String name;
    // 当协程提交到该executor时，更新该时间
    private long lastSubmittedTime;

    // 权重
    private int weight = 5;
    private int currentWeight;

    public PriorityExecutor(long keepAliveTime, TimeUnit timeUnit, int queueSize) {
        queue = new PriorityBlockingQueue<Runnable>(queueSize);
        coreThreadPool = new ThreadPoolExecutor(1, 1, keepAliveTime, timeUnit, queue,
                new NamedThreadFactory("EXECUTOR", false));

        id = ID_SOURCE.incrementAndGet();
        name = PREFIX_NAME + id;

        lastSubmittedTime = System.currentTimeMillis();
    }

    @Override
    protected void initInternal() throws LifecycleException {
        WatchDog.get().addMonitor(new ExecutorMonitor(this));
    }

    @Override
    protected void startInternal() throws LifecycleException {
        // do nothing
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        coreThreadPool.shutdown();
    }

    @Override
    public void execute(Coroutine coroutine) {
        // FIXME 执行协程，缺少中断机制？
        coreThreadPool.execute(coroutine);
        // 最近提交协程的时间
        lastSubmittedTime = System.currentTimeMillis();
    }

    /**
     * 获取executor名称
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 获取当前executor中任务队列的大小
     * @return
     */
    public int getCoroutineSize() {
        return queue.size();
    }

    /**
     * 空闲时间
     * @return
     */
    public long getIdleTime() {
        long idleTime = System.currentTimeMillis() - lastSubmittedTime;
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
