package leemos.jroutine.weave.rewrite;

public class Loop implements Runnable {

    private int i = 0;
    
    @Override
    public void run() {
        print();
    }

    private void print() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        System.out.println(i++);
        print();
    }

}
