# jroutine
## 1、概览
基于Java语言的协程的实现，核心功能包括如下两部分：
- Scheduler: 用于管理协程，根据协程的状态、优先级等因素进行调度，实现上类似操作系统的多级反馈队列(MLFQ)；
- Coroutine: 抽象出的协程的模型：
  - target: 必须实现Runnable接口，为协程承载的实际的业务逻辑；
  - context: 协程的上下文，核心为OperandStack，用于记录协程的当前的操作数栈的数据，当协程重新被调度时，通过OperandStack进行上下文恢复；

## 2、调度器

## 3、协程


## 4、已知问题
- 协程暂停后再恢复时，应该使用scheduler重新调度，现在占用了主线程
- scheduler是否需要考虑缓存亲和性？
- asm部分，应考虑在跳转字节码指令前后也进行weave，目前仅在invoke系列指令前后weave