package leemos.jroutine.schedule.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import leemos.jroutine.AbstractLifecycle;
import leemos.jroutine.exception.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WatchDog
 */
public class WatchDog extends AbstractLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(WatchDog.class);

    private static final String WATCH_DOG_TIMER_NAME = "WATCHDOG-T1";
    private static final WatchDog SINGLETON = new WatchDog();

    private List<Monitor> monitors = new ArrayList<>();
    private Timer timer;
    private TimerTask timerTask;

    public WatchDog() {
        // 创建实例时执行初始化方法
        init();
    }

    @Override
    protected void initInternal() throws LifecycleException {
        timer = new Timer(WATCH_DOG_TIMER_NAME, true);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (monitors) {
                    for (int i = 0; i < monitors.size(); i++) {
                        // 打印monitor采集的信息
                        logger.info(monitors.get(i).collect());
                    }
                }
            }
        };
    }

    @Override
    protected void startInternal() throws LifecycleException {
        // 每10s执行一次监视动作
        timer.schedule(timerTask, 1000, 1000 * 10);
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        timer.cancel();
    }

    /**
     * 获取WatchDog的单例
     * @return
     */
    public static WatchDog get() {
        return SINGLETON;
    }

    /**
     * 增加Monitor
     * @param monitor
     */
    public synchronized void addMonitor(Monitor monitor) {
        monitors.add(monitor);
    }

    /**
     * 删除Monitor
     * @param monitor
     */
    public synchronized void removeMonitor(Monitor monitor) {
        monitors.remove(monitor);
    }
}
