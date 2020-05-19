package org.salp.jroutine.weave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.salp.jroutine.exception.EmptyStackException;

/**
 * Store data in operand stack to the heap.
 * 
 * @author lihao
 * @date 2020-05-05
 */
public class OperandStack implements Serializable {

    private static final long serialVersionUID = -8129811689487823230L;

    private int[] istack;
    private float[] fstack;
    private double[] dstack;
    private long[] lstack;
    private Object[] ostack;
    private Object[] rstack;
    private int itop, ftop, dtop, ltop, otop, rtop;
    protected Runnable runnable;

    public OperandStack(Runnable pRunnable) {
        istack = new int[10];
        fstack = new float[5];
        dstack = new double[5];
        lstack = new long[5];
        ostack = new Object[10];
        rstack = new Object[5];

        runnable = pRunnable;
    }

    public OperandStack(OperandStack parent) {
        istack = new int[parent.istack.length];
        fstack = new float[parent.fstack.length];
        dstack = new double[parent.dstack.length];
        lstack = new long[parent.lstack.length];
        ostack = new Object[parent.ostack.length];
        rstack = new Object[parent.rstack.length];

        runnable = parent.runnable;

        itop = parent.itop;
        ftop = parent.ftop;
        dtop = parent.dtop;
        ltop = parent.ltop;
        otop = parent.otop;
        rtop = parent.rtop;

        System.arraycopy(parent.istack, 0, istack, 0, itop);
        System.arraycopy(parent.fstack, 0, fstack, 0, ftop);
        System.arraycopy(parent.dstack, 0, dstack, 0, dtop);
        System.arraycopy(parent.lstack, 0, lstack, 0, ltop);
        System.arraycopy(parent.ostack, 0, ostack, 0, otop);
        System.arraycopy(parent.rstack, 0, rstack, 0, rtop);
    }

    public boolean hasInt() {
        return itop > 0;
    }

    public int popInt() {
        if (itop == 0) {
            throw new EmptyStackException("pop int");
        }

        return istack[--itop];
    }

    public void pushInt(int i) {
        if (itop == istack.length) {
            int[] hlp = new int[istack.length * 2];
            System.arraycopy(istack, 0, hlp, 0, istack.length);
            istack = hlp;
        }
        istack[itop++] = i;
    }

    public boolean hasFloat() {
        return ftop > 0;
    }

    public float popFloat() {
        if (ftop == 0) {
            throw new EmptyStackException("pop float");
        }
        return fstack[--ftop];
    }

    public void pushFloat(int f) {
        if (ftop == fstack.length) {
            float[] hlp = new float[fstack.length * 2];
            System.arraycopy(fstack, 0, hlp, 0, fstack.length);
            fstack = hlp;
        }
        fstack[ftop++] = f;
    }

    public boolean hasDouble() {
        return dtop > 0;
    }

    public double popDouble() {
        if (dtop == 0) {
            throw new EmptyStackException("pop double");
        }
        return dstack[--dtop];
    }

    public void pushDouble(int d) {
        if (dtop == dstack.length) {
            double[] hlp = new double[dstack.length * 2];
            System.arraycopy(dstack, 0, hlp, 0, dstack.length);
            dstack = hlp;
        }
        dstack[dtop++] = d;
    }

    public boolean hasLong() {
        return ltop > 0;
    }

    public long popLong() {
        if (ltop == 0) {
            throw new EmptyStackException("pop long");
        }
        return lstack[--ltop];
    }

    public void pushLong(int l) {
        if (ltop == lstack.length) {
            long[] hlp = new long[lstack.length * 2];
            System.arraycopy(lstack, 0, hlp, 0, lstack.length);
            lstack = hlp;
        }
        lstack[ltop++] = l;
    }

    public boolean hasObject() {
        return otop > 0;
    }

    public Object popObject() {
        if (otop == 0) {
            throw new EmptyStackException("pop object");
        }
        Object o = ostack[--otop];
        ostack[otop] = null;
        return o;
    }

    public void pushObject(Object o) {
        if (otop == ostack.length) {
            Object[] hlp = new Object[ostack.length * 2];
            System.arraycopy(ostack, 0, hlp, 0, ostack.length);
            ostack = hlp;
        }
        ostack[otop++] = o;
    }

    public boolean hasReference() {
        return rtop > 0;
    }

    public Object popReference() {
        if (rtop == 0) {
            throw new EmptyStackException("pop reference");
        }
        Object r = rstack[--rtop];
        rstack[rtop] = null;
        return r;
    }

    public void pushReference(Object r) {
        if (rtop == rstack.length) {
            Object[] hlp = new Object[rstack.length * 2];
            System.arraycopy(rstack, 0, hlp, 0, rstack.length);
            rstack = hlp;
        }
        rstack[rtop++] = r;
    }

    public boolean isEmpty() {
        return itop == 0 && ltop == 0 && dtop == 0 && ftop == 0 && otop == 0 && rtop == 0;
    }

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

        s.writeObject(runnable);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
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

        runnable = (Runnable) s.readObject();
    }
}
