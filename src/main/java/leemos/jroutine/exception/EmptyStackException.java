package leemos.jroutine.exception;

/**
 * 当操作栈为空时抛出此异常
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
