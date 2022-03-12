package org.salp.jroutine.schedule;

import java.util.Random;

import org.salp.jroutine.Coroutine;

import junit.framework.TestCase;

/**
 * WatchDogTest
 * 
 * @author lihao
 * @date 2020-05-15
 */
public class WatchDogTest extends TestCase {

    private StandardScheduler scheduler;

    @Override
    protected void setUp() throws Exception {
        scheduler = new StandardScheduler();
        scheduler.start();
    }

    public void testSubmit() {
        for (int i = 0; i < 1000; i++) {
            scheduler.submit(new Coroutine(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(new Random().nextInt(20));
                    } catch (InterruptedException e) {
                    }
                }
            }));
        }
    }

}
