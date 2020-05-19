package org.coral.jroutine.exception;

/**
 * Thrown when an exception occurs in the lifecycle.
 * 
 * @author lihao
 * @date 2020-05-12
 */
public class LifecycleException extends RuntimeException {

    private static final long serialVersionUID = -2881351883786620485L;

    public LifecycleException() {
        super();
    }

    public LifecycleException(String s) {
        super(s);
    }

    public LifecycleException(RuntimeException e) {
        super(e);
    }
}
