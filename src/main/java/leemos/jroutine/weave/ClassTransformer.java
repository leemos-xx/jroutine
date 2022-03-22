package leemos.jroutine.weave;

/**
 * 类转换器
 */
public interface ClassTransformer {

    byte[] transform(byte[] classFile);
}
