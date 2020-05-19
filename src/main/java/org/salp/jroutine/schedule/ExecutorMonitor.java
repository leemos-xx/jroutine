package org.salp.jroutine.schedule;

/**
 * executor monitor
 * 
 * @author lihao
 * @date 2020-05-15
 */
public class ExecutorMonitor implements Monitor {

    private PriorityExecutor executor;

    public ExecutorMonitor(PriorityExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String status() {
        return executor.getName() + ": " + "size=" + executor.getTaskSize() + ", idleTime=" + executor.getIdleTime();
    }

}
