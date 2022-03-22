package leemos.jroutine.weave;

import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import leemos.jroutine.Config;

/**
 * 基于ASM的类转换器的实现
 */
public class AsmClassTransformer implements ClassTransformer {

    @Override
    public byte[] transform(byte[] classFile) {
        return transform(new ClassReader(classFile));
    }

    public byte[] transform(ClassReader cr) {
        // 自动计算栈映射帧和操作数栈大小，会影响性能
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        ClassVisitor visitor = new CheckClassAdapter(cw, false);

        if (Config.isDebugEnabled()) {
            // debug模式下打印一些跟踪信息，便于问题排查
            visitor = new TraceClassVisitor(visitor, new PrintWriter(System.out));
        }

        visitor = new JroutineClassAdapter(visitor);

        cr.accept(visitor, 0);

        return cw.toByteArray();
    }

}
