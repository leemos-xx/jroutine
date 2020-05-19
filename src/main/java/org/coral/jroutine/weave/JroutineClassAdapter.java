package org.coral.jroutine.weave;

import org.coral.jroutine.Jroutine;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Add marker interface{@link Jroutine} and enhance effective methods, skip
 * constructor, native and abstract methods.
 * 
 * @author lihao
 * @date 2020-05-08
 */
public class JroutineClassAdapter extends ClassVisitor implements Opcodes {

    private String owner;

    public JroutineClassAdapter(ClassVisitor cv) {
        super(ASM8, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        if ((access & (ACC_ABSTRACT | ACC_INTERFACE)) != 0) {
            cv.visit(version, access, name, signature, superName, interfaces);
            return;
        }

        owner = name;

        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(Type.getInternalName(Jroutine.class))) {
                throw new RuntimeException(name + " has already been enhanced");
            }
        }
        String[] newInterfaces = new String[interfaces.length + 1];
        System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
        // add marker interface
        newInterfaces[newInterfaces.length - 1] = Type.getInternalName(Jroutine.class);

        cv.visit(version, access, name, signature, superName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
            String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        // skip constructor, native and abstract methods
        if (mv != null && !"<init>".equals(name) && ((access & (ACC_ABSTRACT | ACC_NATIVE)) == 0)) {
            mv = new JroutineMethodAnalyzer(owner, mv, access, name, descriptor, signature, exceptions);
        }
        return mv;
    }

}
