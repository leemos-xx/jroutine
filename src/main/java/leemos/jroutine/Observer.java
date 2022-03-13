package leemos.jroutine;

/**
 * 观察者需要实现该接口
 */
public interface Observer<A> {

    void update(A action);
}
