package org.salp.jroutine.exception;

/**
 * 当协程状态存在问题时抛出此异常
 */
public class IllegalCoroutineStateException extends IllegalArgumentException {

    private static final long serialVersionUID = -5772742614941382093L;

    public IllegalCoroutineStateException() {
        super();
    }

    public IllegalCoroutineStateException(String errorMsg) {
        super(errorMsg);
    }
}
