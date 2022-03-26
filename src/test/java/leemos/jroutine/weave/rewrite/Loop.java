package leemos.jroutine.weave.rewrite;

import leemos.jroutine.CoroutineContext;

import java.io.PrintStream;

public class Loop implements Runnable {

    private static PrintStream out = System.out;
    private int i = 0;

    @Override
    public void run() {
        try {
            print(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void print(int i) throws InterruptedException {
        Thread.sleep(500);
        System.out.println(i++);
        print(i);
    }

}
