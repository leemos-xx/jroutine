package leemos.jroutine.weave;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import leemos.jroutine.Enhanced;

/**
 * Class adapter，用于增强被{@link Enhanced}标识后的类。
 * 不增强构造函数、本地方法和抽象方法。
 */
public class JroutineClassAdapter extends ClassVisitor {

    private String className;

    public JroutineClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM8, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        // 如果为抽象类或接口，则不作处理
        if ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE)) != 0) {
            cv.visit(version, access, name, signature, superName, interfaces);
            return;
        }

        className = name;

        // 校验每个class只能被增强一次
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(Type.getInternalName(Enhanced.class))) {
                throw new RuntimeException(name + " has already been enhanced");
            }
        }

        // 在interface中增加{@link Enhanced}标识
        String[] newInterfaces = new String[interfaces.length + 1];
        System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
        newInterfaces[newInterfaces.length - 1] = Type.getInternalName(Enhanced.class);

        cv.visit(version, access, name, signature, superName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
            String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        // 跳过构造函数、抽象方法和本地方法
        if (mv != null && !"<init>".equals(name) && ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0)) {
            mv = new JroutineMethodAnalyzer(className, mv, access, name, descriptor, signature, exceptions);
        }
        return mv;
    }

}
