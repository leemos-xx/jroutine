package org.salp.jroutine.weave;

/**
 * Bytecode transformer for enhancing class files.
 * 
 * @author lihao
 * @date 2020-05-05
 */
public interface ClassTransformer {

    byte[] transform(byte[] classFile);
}
