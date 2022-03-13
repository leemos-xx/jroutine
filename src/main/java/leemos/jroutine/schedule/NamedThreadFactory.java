package leemos.jroutine.schedule;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可命名的线程工厂类
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger ID_SOURCE = new AtomicInteger();
    private static final String FIRST_PREFIX = "JROUTINE-";

    private AtomicInteger threadCounter = new AtomicInteger(1);
    private ThreadGroup group;
    private boolean isDaemon;
    private String namePrefix;

    public NamedThreadFactory(String secondPrefix, boolean daemon) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = FIRST_PREFIX + secondPrefix + "-" + ID_SOURCE.getAndIncrement() + "-T";
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
