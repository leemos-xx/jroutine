package leemos.jroutine.weave.rewrite;

import leemos.jroutine.CoroutineContext;

import java.io.PrintStream;

public class Loop implements Runnable {

    private PrintStream out = System.out;
    private int i = 0;

    @Override
    public void run() {
        String isRestoring = "context: " + CoroutineContext.get().isRestoring;
        out.println(isRestoring);
    }

}
