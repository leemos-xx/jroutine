# Jroutine

jroutine在java语言的体系下，实现了一套非常简化的GPM模型，可以帮助大家理解协程的基础概念。

## Table of Contents
- [快速入门](#快速入门)
- [概要设计](#概要设计)
  - [操作数栈](#操作数栈)
  - [协程控制](#协程控制)
  - [调度策略](#调度策略)
- [已知问题](#已知问题)

## 快速入门
首先看一个完整的协程使用的案例。
```java
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
```
其中在构造`Croutine`的时候，我们必须传入一个实现了`Runnable`接口的类，该类为实际的业务逻辑实现类，在本例中，我们传入`leemos.jroutine.weave.rewrite.Loop`类，该类的逻辑是循环打印一些数据。
```java
public class Loop implements Runnable {

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
        System.out.println(Thread.currentThread().getName() + ": " + i++);
        print(i);
    }

}
```
这段程序执行后的输出如下所示:
```text
1  JROUTINE-EXECUTOR-0-T1: 0
2  JROUTINE-EXECUTOR-0-T1: 1
3  JROUTINE-EXECUTOR-0-T1: 2
4  coroutine suspend for 2s...
5  coroutine resume...
6  JROUTINE-EXECUTOR-1-T1: 3
7  JROUTINE-EXECUTOR-1-T1: 4
8  JROUTINE-EXECUTOR-1-T1: 5
9  JROUTINE-EXECUTOR-1-T1: 6
10 JROUTINE-EXECUTOR-1-T1: 7
```

对于这段输出，我们重点关注两点：
1. 协程上下文切换：可以看到`leemos.jroutine.weave.rewrite.Loop`类的逻辑非常简单，只是不断打印当前的线程名和i的值而已，而i的值每次递归后都会自增1。看日志第3~4行，当i自增为2时，我们挂起了当前的协程，并且等待了2s时间。在第5行进行了协程的恢复，可以看到递归继续进行了，并且i也延续了挂起前的值。
2. 调度器：在协程暂停前，我们可以看到协程运行的线程是`JROUTINE-EXECUTOR-0-T1`，而协程恢复后，运行线程变成了`JROUTINE-EXECUTOR-1-T1`，这是因为默认采用轮询调度策略的原因，后面我们会再支持其它调度策略，如MLFQ。

## 概要设计

### 操作数栈
### 协程控制
### 调度策略

## 已知问题