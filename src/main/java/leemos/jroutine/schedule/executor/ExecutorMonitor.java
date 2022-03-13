package leemos.jroutine.schedule.executor;

/**
 * Executor监视器
 */
public class ExecutorMonitor implements Monitor {

    private Executor executor;

    public ExecutorMonitor(PriorityExecutor executor) {
        this.executor = executor;
    }

    /**
     * 获取
     * @return
     */
    @Override
    public String collect() {
        return executor.getName() + ": " +
                "size=" + executor.getCoroutineSize() + ", " +
                "idleTime=" + executor.getIdleTime() + ", " +
                "weight=" + executor.getWeight() + ", " +
                "currentWeight=" + executor.getCurrentWeight();
    }

}
