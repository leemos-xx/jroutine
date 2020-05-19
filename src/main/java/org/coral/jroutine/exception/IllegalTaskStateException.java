package org.coral.jroutine.exception;

/**
 * Thrown to indicate that a task is not in an appropriate state.
 * 
 * @author lihao
 * @date 2020-04-29
 */
public class IllegalTaskStateException extends IllegalArgumentException {

    private static final long serialVersionUID = -5772742614941382093L;

    public IllegalTaskStateException() {
        super();
    }

    public IllegalTaskStateException(String s) {
        super(s);
    }
}
