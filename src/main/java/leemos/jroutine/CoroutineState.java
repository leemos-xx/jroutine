package leemos.jroutine;

/**
 * 列举协程存在的所有状态
 */
public enum CoroutineState {
    // 新建
    NEW,
    // 处在可运行状态
    RUNNABLE,
    // 处在挂起状态
    SUSPENDING,
    // 处在阻塞状态
    BLOCKED,
    // 处在等待状态
    WAITING,
    // 处在超时等待状态
    TIMED_WAITING,
    // 处在结束状态
    TERMINATED;
}