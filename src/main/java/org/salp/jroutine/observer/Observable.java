package org.salp.jroutine.observer;

import java.util.Vector;

/**
 * Represents an observable object
 * 
 * @author lihao
 * @date 2020-05-05
 */
public class Observable<A> {

    private Vector<Observer<A>> observers;

    public Observable() {
        observers = new Vector<Observer<A>>(1);
    }

    public synchronized void attachObserver(Observer<A> observer) {
        if (observer == null) {
            throw new IllegalArgumentException();
        }

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public synchronized void detachObserver(Observer<A> observer) {
        if (observer == null || !observers.contains(observer)) {
            return;
        }

        observers.remove(observer);
    }

    public synchronized void detachObservers() {
        observers.removeAllElements();
    }

    public synchronized void notifyObservers(A action) {
        observers.stream().forEach(observer -> {
            observer.update(action);
        });
    }

    public synchronized int countObservers() {
        return observers.size();
    }
}
