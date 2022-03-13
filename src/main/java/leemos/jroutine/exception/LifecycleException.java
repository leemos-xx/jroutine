package leemos.jroutine.exception;

/**
 * 当生命周期期间出现异常时抛出
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
