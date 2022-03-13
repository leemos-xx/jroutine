package leemos.jroutine.schedule;

import java.util.concurrent.TimeUnit;

import leemos.jroutine.AbstractLifecycle;
import leemos.jroutine.Coroutine;
import leemos.jroutine.config.Config;
import leemos.jroutine.config.LoadBalanceType;
import leemos.jroutine.exception.LifecycleException;
import leemos.jroutine.schedule.executor.PriorityExecutor;
import leemos.jroutine.schedule.lb.LoadBalancer;
import leemos.jroutine.schedule.executor.Executor;
import leemos.jroutine.schedule.executor.WatchDog;
import leemos.jroutine.schedule.lb.RoundRobinLoadBalancer;
import leemos.jroutine.schedule.lb.WeightRoundRobinLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 标准调度器的实现
 */
public class StandardScheduler extends AbstractLifecycle implements Scheduler<Coroutine> {

    private static final Logger logger = LoggerFactory.getLogger(StandardScheduler.class);

    // executor的默认配置
    private static final long THREAD_KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.HOURS;
    private static final int EXECUTOR_QUEUE_SIZE = 1000;

    // 默认使用轮询的负载均衡器
    private static final LoadBalanceType DEFAULT_LOAD_BALANCER = LoadBalanceType.ROUND_ROBIN;
    private LoadBalancer loadBalancer;

    private Executor<Coroutine>[] executors;

    @Override
    protected void initInternal() throws LifecycleException {
        initExecutors();
        initLoadBalancer();
    }

    /**
     * 初始化executors
     */
    private void initExecutors() {
        // executors的大小，默认为当前节点的cpu核数
        int coreSize = Config.getExecutorsCoreSize() == -1 ? Runtime.getRuntime().availableProcessors()
                : Config.getExecutorsCoreSize();
        long keepAliveTime = Config.getThreadKeepAliveTime() == -1 ? THREAD_KEEP_ALIVE_TIME
                : Config.getThreadKeepAliveTime();
        TimeUnit timeUnit = Config.getKeepAliveTimeUnit() == null ? KEEP_ALIVE_TIME_UNIT
                : Config.getKeepAliveTimeUnit();
        // 任务队列大小，默认为1000
        int queueSize = Config.getExecutorQueueSize() == -1 ? EXECUTOR_QUEUE_SIZE : Config.getExecutorQueueSize();

        executors = new PriorityExecutor[coreSize];
        for (int i = 0; i < coreSize; i++) {
            executors[i] = new PriorityExecutor(keepAliveTime, timeUnit, queueSize);
            executors[i].init();
        }

        logger.info(
                "executor initialized successfully, core_size={}, thread_keep_alive_time={}, keep_alive_time_unit={}, executor_queue_size={}",
                coreSize, keepAliveTime, timeUnit, queueSize);
    }

    private void initLoadBalancer() {
        // 默认使用轮询方式
        LoadBalanceType type = Config.getLoadBalanceType() == null ? DEFAULT_LOAD_BALANCER
                : Config.getLoadBalanceType();
        switch (type) {
            case WEIGHT_ROUND_ROBIN:
                loadBalancer = new WeightRoundRobinLoadBalancer();
                break;
            case ROUND_ROBIN:
            default:
                loadBalancer = new RoundRobinLoadBalancer();
                break;
        }

        logger.info("load balancer initialized successfully, type={}", type);
    }

    @Override
    protected void startInternal() throws LifecycleException {
        for (Executor<Coroutine> executor : executors) {
            executor.start();
        }

        if (Config.isDebugEnabled()) {
            WatchDog.get().start();
        }
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        for (Executor<Coroutine> executor : executors) {
            executor.stop();
        }

        if (Config.isDebugEnabled()) {
            WatchDog.get().stop();
        }
    }

    @Override
    public void submit(Coroutine coroutine) {
        Executor<Coroutine> executor = loadBalancer.select(executors);
        executor.execute(coroutine);
    }

}
