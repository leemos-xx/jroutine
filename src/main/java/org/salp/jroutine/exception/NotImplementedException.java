package org.salp.jroutine.exception;

/**
 * Thrown to indicate that the method is not implemented.
 * 
 * @author lihao
 * @date 2020-05-05
 */
public class NotImplementedException extends RuntimeException {

    private static final long serialVersionUID = 7051609408790026682L;

    public NotImplementedException() {
        super();
    }

    public NotImplementedException(String s) {
        super(s);
    }
}
