package leemos.jroutine.weave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import leemos.jroutine.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分析方法中的变量和指令
 */
public class JroutineMethodAnalyzer extends MethodNode {

    private static final Logger logger = LoggerFactory.getLogger(JroutineMethodAnalyzer.class);

    private final String className;
    private final  List<MethodInsnNode> probablyNewVars = new ArrayList<>();

    protected MethodVisitor mv;
    protected List<Label> buryingLabels = new ArrayList<>();
    protected List<MethodInsnNode> buryingNodes = new ArrayList<>();
    protected int operandStackRecorderVar;
    protected Analyzer<BasicValue> basicAnalyzer;
    protected List<AbstractInsnNode> endNodes = new ArrayList<>();

    public JroutineMethodAnalyzer(String className, MethodVisitor mv, int access, String name, String descriptor,
            String signature, String[] exceptions) {
        super(Opcodes.ASM8, access, name, descriptor, signature, exceptions);
        this.className = className;
        this.mv = mv;
    }

    @Override
    public void visitMethodInsn(int opcodeAndSource, String owner, String name, String descriptor,
            boolean isInterface) {
        if (api < Opcodes.ASM5 && (opcodeAndSource & Opcodes.SOURCE_DEPRECATED) == 0) {
            super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
            return;
        }

        int opcode = opcodeAndSource & ~Opcodes.SOURCE_MASK;
        MethodInsnNode mn = new MethodInsnNode(opcode, owner, name, descriptor, isInterface);
        // navigate to the instruction where the object needs to be created
        if (opcode == Opcodes.INVOKESPECIAL || "<init>".equals(name)) {
            probablyNewVars.add(mn);
        }

        // support multiple extension types, which have different degrees of impact on
        // execution efficiency
        if (isSuspendableInsn(opcode, name)) {
            Label label = new Label();
            super.visitLabel(label);
            buryingLabels.add(label);
            buryingNodes.add(mn);
        }

        instructions.add(mn);
    }
    
    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        // ends of the method
        if (isEndInsn(opcode)) {
            endNodes.add(instructions.getLast());
        }
    }

    @Override
    public void visitEnd() {
        if (instructions.size() == 0 || buryingLabels.size() == 0) {
            accept(mv);
            return;
        }

        operandStackRecorderVar = maxLocals;

        try {
            HashMap<AbstractInsnNode, MethodInsnNode> promotableVars;
            if ((promotableVars = findPromotableVars()).size() > 0) {
                promoteVars(promotableVars);
            }

            //
            basicAnalyzer = new Analyzer<BasicValue>(new SimpleVerifier()) {

                protected Frame<BasicValue> newFrame(final int nLocals, final int nStack) {
                    return new MonitoringFrame(nLocals, nStack);
                }

                protected Frame<BasicValue> newFrame(final Frame src) {
                    return new MonitoringFrame(src);
                }
            };
            basicAnalyzer.analyze(className, this);

            accept(new JroutineMethodAdapter(this));
        } catch (AnalyzerException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            accept(mv);
        }
    }

    // roll back the diffs between asm4 and asm8
    @Override
    protected LabelNode getLabelNode(Label label) {
        if (!(label.info instanceof LabelNode)) {
            label.info = new LabelNode(label);
        }
        return (LabelNode) label.info;
    }

    private HashMap<AbstractInsnNode, MethodInsnNode> findPromotableVars() throws AnalyzerException {
        HashMap<AbstractInsnNode, MethodInsnNode> promotableVars = new HashMap<>();

        Analyzer<SourceValue> sourceAnalyzer = new Analyzer<>(new SourceInterpreter());
        sourceAnalyzer.analyze(className, this);

        Frame<SourceValue>[] frames = sourceAnalyzer.getFrames();
        for (int i = 0; i < probablyNewVars.size(); i++) {
            MethodInsnNode methodInsn = probablyNewVars.get(i);
            Frame<SourceValue> frame = frames[instructions.indexOf(methodInsn)];
            Type[] args = Type.getArgumentTypes(methodInsn.desc);

            SourceValue sourceValue = frame.getStack(frame.getStackSize() - args.length - 1);
            Set<AbstractInsnNode> insns = sourceValue.insns;
            for (AbstractInsnNode insn : insns) {
                if (insn.getOpcode() == Opcodes.NEW) {
                    promotableVars.put(insn, methodInsn);
                } else {
                    if (insn.getOpcode() == Opcodes.DUP) {
                        AbstractInsnNode prevInsn = insn.getPrevious();
                        if (prevInsn.getOpcode() == Opcodes.NEW) {
                            promotableVars.put(prevInsn, methodInsn);
                        }
                    } else if (insn.getOpcode() == Opcodes.SWAP) {
                        AbstractInsnNode insn1 = insn.getPrevious();
                        AbstractInsnNode insn2 = insn1.getPrevious();
                        if (insn2.getOpcode() == Opcodes.NEW && insn1.getOpcode() == Opcodes.DUP_X1) {
                            promotableVars.put(insn2, methodInsn);
                        }
                    }
                }
            }
        }
        return promotableVars;
    }

    private void promoteVars(HashMap<AbstractInsnNode, MethodInsnNode> promotableVars) {
        int updateMaxStack = 0;
        for (Map.Entry<AbstractInsnNode, MethodInsnNode> entry : promotableVars.entrySet()) {
            AbstractInsnNode node1 = entry.getKey();
            AbstractInsnNode node2 = node1.getNext();
            AbstractInsnNode node3 = node2.getNext();

            boolean requireDup = false;
            instructions.remove(node1); // NEW
            if (node2.getOpcode() == Opcodes.DUP) {
                instructions.remove(node2); // DUP
                requireDup = true;
            } else if (node2.getOpcode() == Opcodes.DUP_X1) {
                instructions.remove(node2); // DUP_X1
                instructions.remove(node3); // SWAP
                requireDup = true;
            }

            MethodInsnNode mnode = entry.getValue();
            // FIXME ???
            AbstractInsnNode mn = mnode;

            int varOffset = operandStackRecorderVar + 1;
            Type[] args = Type.getArgumentTypes(mnode.desc);

            if (args.length == 0) {
                InsnList doNew = new InsnList();
                doNew.add(node1);
                if (requireDup) {
                    doNew.add(new InsnNode(Opcodes.DUP));
                }
                instructions.insertBefore(mn, doNew);
                mn = doNew.getLast();
                continue;
            }

            if (args.length == 1 && args[0].getSize() == 1) {
                InsnList doNew = new InsnList();
                doNew.add(node1);
                if (requireDup) {
                    doNew.add(new InsnNode(Opcodes.DUP));
                    doNew.add(new InsnNode(Opcodes.DUP2_X1));
                    doNew.add(new InsnNode(Opcodes.POP2));
                    updateMaxStack = updateMaxStack < 2 ? 2 : updateMaxStack;
                } else {
                    doNew.add(new InsnNode(Opcodes.SWAP));
                }
                instructions.insertBefore(mn, doNew);
                mn = doNew.getLast();
                continue;
            }

            if ((args.length == 1 && args[0].getSize() == 2)
                    || (args.length == 2 && args[0].getSize() == 1 && args[1].getSize() == 1)) {
                final InsnList doNew = new InsnList();
                doNew.add(node1);
                if (requireDup) {
                    doNew.add(new InsnNode(Opcodes.DUP));
                    doNew.add(new InsnNode(Opcodes.DUP2_X2));
                    doNew.add(new InsnNode(Opcodes.POP2));
                    updateMaxStack = updateMaxStack < 2 ? 2 : updateMaxStack;
                } else {
                    doNew.add(new InsnNode(Opcodes.DUP_X2));
                    doNew.add(new InsnNode(Opcodes.POP));
                    updateMaxStack = updateMaxStack < 1 ? 1 : updateMaxStack;
                }
                instructions.insertBefore(mn, doNew);
                mn = doNew.getLast();
                continue;
            }

            InsnList doNew = new InsnList();
            for (int j = args.length - 1; j >= 0; j--) {
                Type type = args[j];

                doNew.add(new VarInsnNode(type.getOpcode(Opcodes.ISTORE), varOffset));
                varOffset += type.getSize();
            }
            maxLocals = varOffset > maxLocals ? varOffset : maxLocals;

            doNew.add(node1);
            if (requireDup) {
                doNew.add(new InsnNode(Opcodes.DUP));
            }

            for (int j = 0; j < args.length; j++) {
                Type type = args[j];
                varOffset -= type.getSize();

                doNew.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), varOffset));

                if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
                    updateMaxStack = updateMaxStack < 1 ? 1 : updateMaxStack;

                    doNew.add(new InsnNode(Opcodes.ACONST_NULL));

                    doNew.add(new VarInsnNode(type.getOpcode(Opcodes.ISTORE), varOffset));
                }
            }

            instructions.insertBefore(mn, doNew);
            mn = doNew.getLast();
        }

        maxStack += updateMaxStack;
    }

    private boolean isSuspendableInsn(int opcode, String name) {
        if (Config.getExtensionType() == ExtensionType.METHOD && isMethodInsn(opcode, name)) {
            return true;
        }
        if (Config.getExtensionType() == ExtensionType.METHOD_AND_LOOP
                && (isMethodInsn(opcode, name) || isLoopInsn(opcode))) {
            return true;
        }
        return false;
    }

    private boolean isEndInsn(int opcode) {
        return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
    }

    private boolean isMethodInsn(int opcode, String name) {
        return opcode == Opcodes.INVOKEINTERFACE || opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKESTATIC
                || (opcode == Opcodes.INVOKESPECIAL && !"<init>".equals(name));
    }

    // FIXME
    private boolean isLoopInsn(int opcode) {
        return true;
    }

}
