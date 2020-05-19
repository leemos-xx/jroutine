package org.salp.jroutine;

/**
 * List all possible states of the task.
 * 
 * @author lihao
 * @date 2020-05-05
 */
public enum TaskState {
    NEW, 
    RUNNABLE, 
    SUSPENDING, 
    BLOCKED, 
    WAITING, 
    TIMED_WAITING, 
    TERMINATED;
}