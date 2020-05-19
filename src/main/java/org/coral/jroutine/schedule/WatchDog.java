package org.coral.jroutine.schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.coral.jroutine.AbstractLifecycle;
import org.coral.jroutine.exception.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * for internal monitoring.
 * 
 * @author lihao
 * @date 2020-05-15
 */
public class WatchDog extends AbstractLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(WatchDog.class);
    private static final WatchDog WATCH_DOG = new WatchDog();

    private SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
    private Timer timer;
    private TimerTask task;
    private List<Monitor> monitors;

    public WatchDog() {
        init();
    }

    public static WatchDog me() {
        return WATCH_DOG;
    }

    public void addMonitor(Monitor monitor) {
        monitors.add(monitor);
    }

    public void remove(Monitor monitor) {
        monitors.remove(monitor);
    }

    @Override
    protected void initInternal() throws LifecycleException {
        monitors = new ArrayList<Monitor>();

        timer = new Timer("JROUTINE-WATCHDOG-T1", true);
        task = new TimerTask() {
            
            @Override
            public void run() {
                synchronized (monitors) {
                    logger.info("Time:" + formatter.format(new Date()));
                    for (int i = 0; i < monitors.size(); i++) {
                        // just print it out
                        logger.info(monitors.get(i).status());
                    }
                }
            }
        };
    }

    @Override
    protected void startInternal() throws LifecycleException {
        timer.schedule(task, 1000, 1000 * 10);
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        monitors = new ArrayList<Monitor>();
        timer.cancel();
    }
}
