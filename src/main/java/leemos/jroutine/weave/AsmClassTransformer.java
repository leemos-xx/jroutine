package leemos.jroutine.weave;

import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import leemos.jroutine.config.Configs;

/**
 * Bytecode transformer via asm.
 * 
 * @author lihao
 * @date 2020-05-08
 */
public class AsmClassTransformer implements ClassTransformer {

    @Override
    public byte[] transform(byte[] classFile) {
        return transform(new ClassReader(classFile));
    }

    public byte[] transform(ClassReader cr) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        // make sure that the transformed bytecode is sane
        ClassVisitor visitor = new CheckClassAdapter(cw, false);
        // trace the classes
        if (Configs.isDebugEnabled()) {
            visitor = new TraceClassVisitor(visitor, new PrintWriter(System.out));
        }
        // enhance bytecode
        visitor = new JroutineClassAdapter(visitor);

        cr.accept(visitor, 0);

        return cw.toByteArray();
    }

}
