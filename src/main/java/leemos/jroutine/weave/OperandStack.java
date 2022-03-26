package leemos.jroutine.weave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import leemos.jroutine.exception.EmptyStackException;

/**
 * OperandStack是操作数栈的映射，在协程上下文切换时，用来暂存或恢复线程栈帧中的操作数栈
 */
public abstract class OperandStack implements Serializable {

    private static final long serialVersionUID = -8129811689487823230L;

    // astack比较特殊，它并不是用来存储程序运行过程中产生的数据，而是用来作为内部逻辑流转的锚点
    private int[] astack;

    // 如下的stack均用于基本数据类型和引用数据类型的存储
    private int[] istack;
    private float[] fstack;
    private double[] dstack;
    private long[] lstack;
    private Object[] ostack;
    private Object[] rstack;
    private int atop, itop, ftop, dtop, ltop, otop, rtop;

    public OperandStack() {
        astack = new int[10];
        istack = new int[10];
        fstack = new float[5];
        dstack = new double[5];
        lstack = new long[5];
        ostack = new Object[10];
        rstack = new Object[5];
    }

    public OperandStack(OperandStack parent) {
        astack = new int[parent.istack.length];
        istack = new int[parent.istack.length];
        fstack = new float[parent.fstack.length];
        dstack = new double[parent.dstack.length];
        lstack = new long[parent.lstack.length];
        ostack = new Object[parent.ostack.length];
        rstack = new Object[parent.rstack.length];

        atop = parent.atop;
        itop = parent.itop;
        ftop = parent.ftop;
        dtop = parent.dtop;
        ltop = parent.ltop;
        otop = parent.otop;
        rtop = parent.rtop;

        System.arraycopy(parent.astack, 0, astack, 0, atop);
        System.arraycopy(parent.istack, 0, istack, 0, itop);
        System.arraycopy(parent.fstack, 0, fstack, 0, ftop);
        System.arraycopy(parent.dstack, 0, dstack, 0, dtop);
        System.arraycopy(parent.lstack, 0, lstack, 0, ltop);
        System.arraycopy(parent.ostack, 0, ostack, 0, otop);
        System.arraycopy(parent.rstack, 0, rstack, 0, rtop);
    }

    /**
     * 是否存在锚点
     * @return
     */
    public boolean hasAnchor() {
        return atop > 0;
    }

    /**
     * 从锚点栈中弹出数据，当锚点栈无数据可弹出时，认为恢复已完成
     * @return
     */
    public int popAnchor() {
        if (atop == 0) {
            restored();
            return -1;
        }

        return astack[--atop];
    }

    /**
     * 压入锚点
     * @param a
     */
    public void pushAnchor(int a) {
        if (atop == astack.length) {
            int[] hlp = new int[astack.length * 2];
            System.arraycopy(astack, 0, hlp, 0, astack.length);
            astack = hlp;
        }
        astack[atop++] = a;
    }

    /**
     * 将操作数栈置为已恢复状态
     */
    public abstract void restored();

    /**
     * 操作数栈中是否有int数据
     * @return
     */
    public boolean hasInt() {
        return itop > 0;
    }

    /**
     * 弹出int数据
     * @return
     */
    public int popInt() {
        if (itop == 0) {
            throw new EmptyStackException("pop int");
        }

        return istack[--itop];
    }

    /**
     * 压入int数据
     * @param i
     */
    public void pushInt(int i) {
        if (itop == istack.length) {
            int[] hlp = new int[istack.length * 2];
            System.arraycopy(istack, 0, hlp, 0, istack.length);
            istack = hlp;
        }
        istack[itop++] = i;
    }

    /**
     * 操作数栈中是否有float数据
     * @return
     */
    public boolean hasFloat() {
        return ftop > 0;
    }

    /**
     * 弹出float数据
     * @return
     */
    public float popFloat() {
        if (ftop == 0) {
            throw new EmptyStackException("pop float");
        }
        return fstack[--ftop];
    }

    /**
     * 压入float数据
     * @param f
     */
    public void pushFloat(float f) {
        if (ftop == fstack.length) {
            float[] hlp = new float[fstack.length * 2];
            System.arraycopy(fstack, 0, hlp, 0, fstack.length);
            fstack = hlp;
        }
        fstack[ftop++] = f;
    }

    /**
     * 操作数栈中是否有double数据
     * @return
     */
    public boolean hasDouble() {
        return dtop > 0;
    }

    /**
     * 弹出double数据
     * @return
     */
    public double popDouble() {
        if (dtop == 0) {
            throw new EmptyStackException("pop double");
        }
        return dstack[--dtop];
    }

    /**
     * 压入double数据
     * @param d
     */
    public void pushDouble(double d) {
        if (dtop == dstack.length) {
            double[] hlp = new double[dstack.length * 2];
            System.arraycopy(dstack, 0, hlp, 0, dstack.length);
            dstack = hlp;
        }
        dstack[dtop++] = d;
    }

    /**
     * 操作数栈中是否有long数据
     * @return
     */
    public boolean hasLong() {
        return ltop > 0;
    }

    /**
     * 弹出long数据
     * @return
     */
    public long popLong() {
        if (ltop == 0) {
            throw new EmptyStackException("pop long");
        }
        return lstack[--ltop];
    }

    /**
     * 压入long数据
     * @param l
     */
    public void pushLong(int l) {
        if (ltop == lstack.length) {
            long[] hlp = new long[lstack.length * 2];
            System.arraycopy(lstack, 0, hlp, 0, lstack.length);
            lstack = hlp;
        }
        lstack[ltop++] = l;
    }

    /**
     * 操作数栈中是否有object数据
     * @return
     */
    public boolean hasObject() {
        return otop > 0;
    }

    /**
     * 弹出object
     * @return
     */
    public Object popObject() {
        if (otop == 0) {
            throw new EmptyStackException("pop object");
        }

        Object o = ostack[--otop];
        ostack[otop] = null;

        return o;
    }

    /**
     * 压入object
     * @param o
     */
    public void pushObject(Object o) {
        if (otop == ostack.length) {
            Object[] hlp = new Object[ostack.length * 2];
            System.arraycopy(ostack, 0, hlp, 0, ostack.length);
            ostack = hlp;
        }
        ostack[otop++] = o;
    }

    /**
     * 操作数栈中是否有reference
     * @return
     */
    public boolean hasReference() {
        return rtop > 0;
    }

    /**
     * 弹出reference
     * @return
     */
    public Object popReference() {
        if (rtop == 0) {
            throw new EmptyStackException("pop reference");
        }
        Object r = rstack[--rtop];
        rstack[rtop] = null;
        return r;
    }

    /**
     * 压入reference
     * @param r
     */
    public void pushReference(Object r) {
        if (rtop == rstack.length) {
            Object[] hlp = new Object[rstack.length * 2];
            System.arraycopy(rstack, 0, hlp, 0, rstack.length);
            rstack = hlp;
        }
        rstack[rtop++] = r;
    }

    /**
     * 操作数栈是否为空
     * @return
     */
    public boolean isEmpty() {
        return atop == 0 && itop == 0 && ltop == 0 && dtop == 0 && ftop == 0 && otop == 0 && rtop == 0;
    }

    /**
     * 操作数栈是否可序列化，会判断栈中关联的数据是否可序列化
     * @return
     */
    public boolean isSerializable() {
        for (int i = 0; i < otop; i++) {
            final Object o = ostack[i];
            if (!(o instanceof Serializable)) {
                return false;
            }
        }
        for (int i = 0; i < rtop; i++) {
            final Object r = rstack[i];
            if (!(r instanceof Serializable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("a[").append(atop).append("]\n");
        sb.append("i[").append(itop).append("]\n");
        sb.append("f[").append(ftop).append("]\n");
        sb.append("d[").append(dtop).append("]\n");
        sb.append("l[").append(ltop).append("]\n");
        sb.append("o[").append(otop).append("]\n");
        for (int i = 0; i < otop; i++) {
            sb.append(' ').append(i).append(": ");
            sb.append(ostack[i].getClass().getName());
            sb.append("@").append(ostack[i].hashCode()).append('\n');
        }
        sb.append("r[").append(rtop).append("]\n");
        for (int i = 0; i < rtop; i++) {
            sb.append(' ').append(i).append(": ");
            sb.append(rstack[i].getClass().getName());
            sb.append("@").append(rstack[i].hashCode()).append('\n');
        }

        return sb.toString();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.writeObject(atop);
        for (int i = 0; i < atop; i++) {
            s.writeInt(astack[i]);
        }

        s.writeInt(itop);
        for (int i = 0; i < itop; i++) {
            s.writeInt(istack[i]);
        }

        s.writeInt(ftop);
        for (int i = 0; i < ftop; i++) {
            s.writeDouble(fstack[i]);
        }

        s.writeInt(dtop);
        for (int i = 0; i < dtop; i++) {
            s.writeDouble(dstack[i]);
        }

        s.writeInt(ltop);
        for (int i = 0; i < ltop; i++) {
            s.writeLong(lstack[i]);
        }

        s.writeInt(otop);
        for (int i = 0; i < otop; i++) {
            s.writeObject(ostack[i]);
        }

        s.writeInt(rtop);
        for (int i = 0; i < rtop; i++) {
            s.writeObject(rstack[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        atop = s.readInt();
        astack = new int[atop];
        for (int i = 0; i < atop; i++) {
            astack[i] = s.readInt();
        }

        itop = s.readInt();
        istack = new int[itop];
        for (int i = 0; i < itop; i++) {
            istack[i] = s.readInt();
        }

        ftop = s.readInt();
        fstack = new float[ftop];
        for (int i = 0; i < ftop; i++) {
            fstack[i] = s.readFloat();
        }

        dtop = s.readInt();
        dstack = new double[dtop];
        for (int i = 0; i < dtop; i++) {
            dstack[i] = s.readDouble();
        }

        ltop = s.readInt();
        lstack = new long[ltop];
        for (int i = 0; i < ltop; i++) {
            lstack[i] = s.readLong();
        }

        otop = s.readInt();
        ostack = new Object[otop];
        for (int i = 0; i < otop; i++) {
            ostack[i] = s.readObject();
        }

        rtop = s.readInt();
        rstack = new Object[rtop];
        for (int i = 0; i < rtop; i++) {
            rstack[i] = s.readObject();
        }
    }
}
