package leemos.jroutine.exception;

/**
 * Thrown to indicate that the stack is empty.
 * 
 * @author lihao
 * @date 2020-05-05
 */
public class EmptyStackException extends RuntimeException {

    private static final long serialVersionUID = 7051609408790026682L;

    public EmptyStackException() {
        super();
    }

    public EmptyStackException(String s) {
        super(s);
    }
}
