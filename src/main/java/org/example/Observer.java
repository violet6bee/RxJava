package org.example;

public interface Observer<T> {
    void onSubscribe(Disposable d);
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
}