package leemos.jroutine.weave;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public final class MonitoringFrame extends Frame<BasicValue> {

    private List<Integer> monitored;

    public MonitoringFrame(Frame<BasicValue> arg0) {
        super(arg0);
    }

    public MonitoringFrame(int arg0, int arg1) {
        super(arg0, arg1);
        monitored = new LinkedList<>();
    }

    @Override
    public void execute(AbstractInsnNode insn, Interpreter<BasicValue> interpreter)
            throws AnalyzerException {

        boolean never = false;
        if (never) {
            super.execute(insn, interpreter);
            return;
        }

        int insnOpcode = insn.getOpcode();

        if (insnOpcode == Opcodes.MONITORENTER || insnOpcode == Opcodes.MONITOREXIT) {
            BasicValue pop = pop();
            interpreter.unaryOperation(insn, pop);

            int local = -1;
            for (int i = 0; i < getLocals(); i++) {
                if (getLocal(i) == pop) local = i;
            }

            if (local > -1) {
                if (insnOpcode == Opcodes.MONITORENTER) {
                    monitorEnter(local);
                } else {
                    monitorExit(local);
                }
            }

        } else {
            super.execute(insn, interpreter);
        }
    }

    @Override
    public Frame<BasicValue> init(Frame frame) {
        super.init(frame);
        if (frame instanceof MonitoringFrame) {
            monitored = new LinkedList<>(MonitoringFrame.class.cast(frame).monitored);
        } else {
            monitored = new LinkedList<>();
        }
        return this;
    }

    public int[] getMonitored() {
        int[] res = new int[monitored.size()];
        for (int i = 0; i < monitored.size(); i++) {
            res[i] = monitored.get(i);
        }
        return res;
    }

    public void monitorEnter(int local) {
        monitored.add(local);
    }

    public void monitorExit(int local) {
        int index = monitored.lastIndexOf(local);
        if (index == -1) {

        } else {
            monitored.remove(index);
        }
    }

}
