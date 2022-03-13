package leemos.jroutine.exception;

/**
 * 当尝试调度一个未经字节码增强的类时抛出此异常
 */
public class NonEnhancedClassException extends IllegalArgumentException {

    private static final long serialVersionUID = 3084170063439264232L;

    public NonEnhancedClassException() {
        super();
    }

    public NonEnhancedClassException(String s) {
        super(s);
    }
}
