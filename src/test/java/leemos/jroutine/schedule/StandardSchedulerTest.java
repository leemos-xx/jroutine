package leemos.jroutine.schedule;

import java.net.MalformedURLException;
import java.net.URL;

import leemos.jroutine.Coroutine;
import leemos.jroutine.weave.AsmClassTransformer;
import leemos.jroutine.weave.WeaverClassLoader;

import junit.framework.TestCase;

/**
 * StandardSchedulerTest
 * 
 * @author lihao
 * @date 2020-05-14
 */
public class StandardSchedulerTest extends TestCase {

    private StandardScheduler scheduler;

    @Override
    protected void setUp() throws Exception {
        scheduler = new StandardScheduler();
        scheduler.start();
    }

    public void testSubmit() throws MalformedURLException, InterruptedException {
        @SuppressWarnings("resource")
        WeaverClassLoader classLoader = new WeaverClassLoader(new URL[] {}, new AsmClassTransformer());
        try {
            Class<?> clazz = classLoader.loadClass("leemos.jroutine.weave.rewrite.Loop");
            Coroutine coroutine = new Coroutine((Runnable) clazz.newInstance());
            scheduler.submit(coroutine);

            Thread.sleep(2000);
            System.out.println("suspend");
            coroutine.suspend();
            Thread.sleep(4000);
            System.out.println("resume");
            coroutine.resume();

            Thread.sleep(1000);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}
