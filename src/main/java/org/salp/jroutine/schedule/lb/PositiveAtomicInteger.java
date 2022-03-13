package org.salp.jroutine.schedule.lb;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 始终返回正整数的原子类的包装类
 */
public final class PositiveAtomicInteger {

    private static final int MASK = 0x7FFFFFFF;
    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    public int getAndIncrement() {
        return atomicInteger.getAndIncrement() & MASK;
    }
}
