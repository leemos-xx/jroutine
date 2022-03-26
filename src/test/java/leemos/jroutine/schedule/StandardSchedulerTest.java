package leemos.jroutine.schedule;

import java.net.MalformedURLException;
import java.net.URL;

import leemos.jroutine.Coroutine;
import leemos.jroutine.weave.AsmClassTransformer;
import leemos.jroutine.weave.ClassTransformer;
import leemos.jroutine.weave.WeaverClassLoader;

import junit.framework.TestCase;

/**
 * StandardSchedulerTest
 *
 * @author lihao
 * @date 2020-05-14
 */
public class StandardSchedulerTest extends TestCase {

    public void testSubmit() throws Exception {
        // 启动调度器
        StandardScheduler scheduler = new StandardScheduler();
        scheduler.start();

        // 使用ASM类加载器加载业务资源
        WeaverClassLoader classLoader = new WeaverClassLoader(new URL[]{}, new AsmClassTransformer());
        Class<?> clazz = classLoader.loadClass("leemos.jroutine.weave.rewrite.Loop");

        // 构造协程
        Coroutine coroutine = new Coroutine((Runnable) clazz.newInstance());

        // 开始调度协程
        scheduler.submit(coroutine);

        // 协程挂起
        Thread.sleep(2000);
        System.out.println("coroutine suspend for 2s...");
        scheduler.suspend(coroutine);

        // 协程恢复
        Thread.sleep(2000);
        System.out.println("coroutine resume...");
        scheduler.resume(coroutine);


        Thread.sleep(Integer.MAX_VALUE);

    }

}
