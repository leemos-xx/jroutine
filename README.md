# jroutine
基于Java的协程的概念实现。

## 已知问题
- 协程暂停后再恢复时，应该使用scheduler重新调度，现在占用了主线程
- scheduler调度应使用分级反馈队列
- scheduler是否需要考虑缓存亲和性？
- asm部分，应考虑在跳转字节码指令前后也进行weave，目前仅在invoke系列指令前后weave