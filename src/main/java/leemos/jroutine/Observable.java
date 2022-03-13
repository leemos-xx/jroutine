package leemos.jroutine;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 观察者模式，在Jroutine中目前考虑适用于对协程状态的观察
 */
public abstract class Observable<A> {

    private CopyOnWriteArrayList<Observer<A>> observers;

    public Observable() {
        observers = new CopyOnWriteArrayList<Observer<A>>();
    }

    /**
     * 通知所有观察者
     * @param action
     */
    public synchronized void notifyObservers(A action) {
        observers.stream().forEach(observer -> {
            observer.update(action);
        });
    }

    /**
     * 添加观察者
     * @param observer
     */
    public synchronized void attachObserver(Observer<A> observer) {
        if (observer == null) {
            throw new IllegalArgumentException();
        }

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * 移除观察者
     * @param observer
     */
    public synchronized void detachObserver(Observer<A> observer) {
        if (observer == null || !observers.contains(observer)) {
            return;
        }

        observers.remove(observer);
    }

    /**
     * 清空观察者
     */
    public void detachObservers() {
        observers.clear();
    }

    /**
     * 观察者数量
     * @return
     */
    public int countObservers() {
        return observers.size();
    }
}
