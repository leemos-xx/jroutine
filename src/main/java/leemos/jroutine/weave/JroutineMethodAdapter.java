package leemos.jroutine.weave;

import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import leemos.jroutine.CoroutineContext;

import static org.objectweb.asm.Opcodes.*;

/**
 * JroutineMethodAdapter
 */
public class JroutineMethodAdapter extends MethodVisitor {

    private static final String RECORDER = Type.getInternalName(CoroutineContext.class);
    private static final String POP_METHOD = "pop";
    private static final String PUSH_METHOD = "push";

    private JroutineMethodAnalyzer analyzer;
    protected List<Label> buryingLabels;
    protected List<MethodInsnNode> buryingNodes;
    protected List<AbstractInsnNode> endNodes;
    protected int operandStackRecorderVar;

    private Label startLabel = new Label();
    private int currentIndex = 0;
    private Frame<BasicValue> currentFrame = null;

    public JroutineMethodAdapter(JroutineMethodAnalyzer analyzer) {
        super(ASM8, analyzer.mv);
        this.analyzer = analyzer;
        this.buryingLabels = analyzer.buryingLabels;
        this.buryingNodes = analyzer.buryingNodes;
        this.endNodes = analyzer.endNodes;
        this.operandStackRecorderVar = analyzer.operandStackRecorderVar;
    }

    @Override
    public void visitCode() {
        mv.visitCode();

        int fsize = buryingLabels.size();
        Label[] restoreLabels = new Label[fsize];
        for (int i = 0; i < fsize; i++) {
            restoreLabels[i] = new Label();
        }

        Label l0 = new Label();

        // CoroutineContext context = CoroutineContext.get();
        mv.visitMethodInsn(INVOKESTATIC, RECORDER, "get", "()L" + RECORDER + ";", false);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, operandStackRecorderVar);
        mv.visitLabel(startLabel);

        // if (context != null && !context.isRestoring)
        mv.visitJumpInsn(IFNULL, l0);
        mv.visitVarInsn(ALOAD, operandStackRecorderVar);
        mv.visitFieldInsn(GETFIELD, RECORDER, "isRestoring", "Z");
        mv.visitJumpInsn(IFEQ, l0);

        // context.popInt()
        mv.visitVarInsn(ALOAD, operandStackRecorderVar);
        mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, POP_METHOD + "Int", "()I", false);
        mv.visitTableSwitchInsn(0, fsize - 1, l0, restoreLabels);

        for (int i = 0; i < fsize; i++) {
            Label frameLabel = buryingLabels.get(i);
            mv.visitLabel(restoreLabels[i]);

            MethodInsnNode mn = buryingNodes.get(i);
            int index = analyzer.instructions.indexOf(mn);
            Frame<BasicValue> frame = analyzer.basicAnalyzer.getFrames()[index];

            int localSize = frame.getLocals();
            for (int j = localSize - 1; j >= 0; j--) {
                BasicValue value = frame.getLocal(j);
                if (isNull(value)) {
                    mv.visitInsn(ACONST_NULL);
                    mv.visitVarInsn(ASTORE, j);
                } else if (value == BasicValue.UNINITIALIZED_VALUE) {

                } else if (value == BasicValue.RETURNADDRESS_VALUE) {

                } else {
                    mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                    Type type = value.getType();
                    if (value.isReference()) {
                        mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, POP_METHOD + "Object", "()Ljava/lang/Object;",
                                false);
                        if (type.getDescriptor().charAt(0) == '[') {
                            mv.visitTypeInsn(CHECKCAST, type.getDescriptor());
                        } else {
                            mv.visitTypeInsn(CHECKCAST, type.getInternalName());
                        }
                        mv.visitVarInsn(ASTORE, j);
                    } else {
                        mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, getPopMethod(type), "()" + type.getDescriptor(),
                                false);
                        mv.visitVarInsn(type.getOpcode(ISTORE), j);
                    }
                }
            }

            if (frame instanceof MonitoringFrame) {
                int[] monitoredLocals = ((MonitoringFrame) frame).getMonitored();
                for (int monitoredLocal : monitoredLocals) {
                    mv.visitVarInsn(ALOAD, monitoredLocal);
                    mv.visitInsn(MONITORENTER);
                }
            }

            int argSize = Type.getArgumentTypes(mn.desc).length;
            int ownerSize = mn.getOpcode() == INVOKESTATIC ? 0 : 1;
            int initSize = mn.name.equals("<init>") ? 2 : 0;
            int stackSize = frame.getStackSize();
            for (int j = 0; j < stackSize - argSize - ownerSize - initSize; j++) {
                BasicValue value = frame.getStack(j);
                if (isNull(value)) {
                    mv.visitInsn(ACONST_NULL);
                } else if (value == BasicValue.UNINITIALIZED_VALUE) {

                } else if (value == BasicValue.RETURNADDRESS_VALUE) {

                } else if (value.isReference()) {
                    mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                    mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, POP_METHOD + "Object", "()Ljava/lang/Object;", false);
                    mv.visitTypeInsn(CHECKCAST, value.getType().getInternalName());
                } else {
                    Type type = value.getType();
                    mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                    mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, getPopMethod(type), "()" + type.getDescriptor(), false);
                }
            }

            if (mn.getOpcode() != INVOKESTATIC) {
                BasicValue value = frame.getStack(stackSize - argSize - 1);
                if (isNull(value)) {
                    mv.visitInsn(ACONST_NULL);
                } else {
                    mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                    mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, POP_METHOD + "Reference", "()Ljava/lang/Object;",
                            false);
                    mv.visitTypeInsn(CHECKCAST, value.getType().getInternalName());
                }
            }

            for (Type paramType : Type.getArgumentTypes(mn.desc)) {
                pushDefault(paramType);
            }

            mv.visitJumpInsn(GOTO, frameLabel);
        }

        for (int i = 0; i < endNodes.size(); i++) {
            InsnList insns = new InsnList();
            insns.add(new MethodInsnNode(INVOKESTATIC, RECORDER, "get", "()L" + RECORDER + ";", false));
            insns.add(new MethodInsnNode(INVOKEVIRTUAL, RECORDER, "done", "()V", false));

            analyzer.instructions.insertBefore(endNodes.get(i), insns);
        }

        mv.visitLabel(l0);
    }

    @Override
    public void visitLabel(Label label) {
        if (currentIndex < buryingLabels.size() && label == buryingLabels.get(currentIndex)) {
            int i = analyzer.instructions.indexOf(buryingNodes.get(currentIndex));
            currentFrame = analyzer.basicAnalyzer.getFrames()[i];
        }
        mv.visitLabel(label);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

        if (currentFrame != null) {
            Label fl = new Label();

            mv.visitVarInsn(ALOAD, operandStackRecorderVar);
            mv.visitJumpInsn(IFNULL, fl);
            mv.visitVarInsn(ALOAD, operandStackRecorderVar);
            mv.visitFieldInsn(GETFIELD, RECORDER, "isCapturing", "Z");
            mv.visitJumpInsn(IFEQ, fl);

            Type returnType = Type.getReturnType(descriptor);
            boolean hasReturn = returnType != Type.VOID_TYPE;
            if (hasReturn) {
                mv.visitInsn(returnType.getSize() == 1 ? POP : POP2);
            }

            Type[] params = Type.getArgumentTypes(descriptor);
            int argSize = params.length;
            int ownerSize = opcode == INVOKESTATIC ? 0 : 1;
            int ssize = currentFrame.getStackSize() - argSize - ownerSize;
            for (int i = ssize - 1; i >= 0; i--) {
                BasicValue value = (BasicValue) currentFrame.getStack(i);
                if (isNull(value)) {
                    mv.visitInsn(POP);
                } else if (value == BasicValue.UNINITIALIZED_VALUE) {

                } else if (value.isReference()) {
                    mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                    mv.visitInsn(SWAP);
                    mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, PUSH_METHOD + "Object", "(Ljava/lang/Object;)V", false);
                } else {
                    Type type = value.getType();
                    if (type.getSize() > 1) {
                        mv.visitInsn(ACONST_NULL);
                        mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                        mv.visitInsn(DUP2_X2);
                        mv.visitInsn(POP2);
                        mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, getPushMethod(type),
                                "(" + type.getDescriptor() + ")V", false);
                        mv.visitInsn(POP);
                    } else {
                        mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                        mv.visitInsn(SWAP);
                        mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, getPushMethod(type),
                                "(" + type.getDescriptor() + ")V", false);
                    }
                }
            }

            if (!((analyzer.access & ACC_STATIC) > 0)) {
                mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, PUSH_METHOD + "Reference", "(Ljava/lang/Object;)V", false);
            }

            int fsize = currentFrame.getLocals();
            for (int j = 0; j < fsize; j++) {
                BasicValue value = (BasicValue) currentFrame.getLocal(j);
                if (isNull(value)) {

                } else if (value == BasicValue.UNINITIALIZED_VALUE) {

                } else if (value.isReference()) {
                    mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                    mv.visitVarInsn(ALOAD, j);
                    mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, PUSH_METHOD + "Object", "(Ljava/lang/Object;)V", false);
                } else {
                    mv.visitVarInsn(ALOAD, operandStackRecorderVar);
                    Type type = value.getType();
                    mv.visitVarInsn(type.getOpcode(ILOAD), j);
                    mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, getPushMethod(type), "(" + type.getDescriptor() + ")V",
                            false);
                }
            }

            mv.visitVarInsn(ALOAD, operandStackRecorderVar);
            if (currentIndex >= 128) {
                mv.visitIntInsn(SIPUSH, currentIndex);
            } else {
                mv.visitIntInsn(BIPUSH, currentIndex);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, RECORDER, "pushInt", "(I)V", false);

            if (currentFrame instanceof MonitoringFrame) {
                int[] monitoredLocals = ((MonitoringFrame) currentFrame).getMonitored();
                for (int monitoredLocal : monitoredLocals) {
                    mv.visitVarInsn(ALOAD, monitoredLocal);
                    mv.visitInsn(MONITOREXIT);
                }
            }

            Type methodReturnType = Type.getReturnType(analyzer.desc);
            pushDefault(methodReturnType);
            mv.visitInsn(methodReturnType.getOpcode(IRETURN));
            mv.visitLabel(fl);

            currentIndex++;
            currentFrame = null;
        }

    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        Label endLabel = new Label();
        mv.visitLabel(endLabel);

        mv.visitLocalVariable("context", "L" + RECORDER + ";", null, startLabel, endLabel, operandStackRecorderVar);

        mv.visitMaxs(0, 0);
    }

    private String getPopMethod(Type type) {
        return POP_METHOD + suffix(type);
    }

    private String getPushMethod(Type type) {
        return PUSH_METHOD + suffix(type);
    }

    private boolean isNull(BasicValue value) {
        if (null == value) {
            return true;
        }
        if (!value.isReference()) {
            return false;
        }
        Type type = value.getType();
        return "Lnull;".equals(type.getDescriptor());
    }

    private void pushDefault(Type type) {
        switch (type.getSort()) {
        case Type.VOID:
            break;
        case Type.DOUBLE:
            mv.visitInsn(DCONST_0);
            break;
        case Type.LONG:
            mv.visitInsn(LCONST_0);
            break;
        case Type.FLOAT:
            mv.visitInsn(FCONST_0);
            break;
        case Type.OBJECT:
        case Type.ARRAY:
            mv.visitInsn(ACONST_NULL);
            break;
        default:
            mv.visitInsn(ICONST_0);
            break;
        }
    }

    private static String suffix(Type type) {
        String[] suffixes = { "Object", // 0 void
                "Int", // 1 boolean
                "Int", // 2 char
                "Int", // 3 byte
                "Int", // 4 short
                "Int", // 5 int
                "Float", // 6 float
                "Long", // 7 long
                "Double", // 8 double
                "Object", // 9 array
                "Object", // 10 object
        };

        return suffixes[type.getSort()];
    }
}
