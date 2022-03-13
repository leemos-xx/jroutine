package leemos.jroutine.schedule;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * named thread factory.
 * 
 * @author lihao
 * @date 2020-05-12
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_COUNTER = new AtomicInteger();

    private final AtomicInteger threadCounter = new AtomicInteger(1);
    private final String JROUTINE_PREFIX = "JROUTINE-";

    private ThreadGroup group;
    private boolean isDaemon;
    private String namePrefix;

    public NamedThreadFactory(String secondPrefix, boolean daemon) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = JROUTINE_PREFIX + secondPrefix + "-" + POOL_COUNTER.getAndIncrement() + "-T";
        isDaemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadCounter.getAndIncrement(), 0);
        t.setDaemon(isDaemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
