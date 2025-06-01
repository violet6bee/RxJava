package org.example;

public interface ObservableOnSubscribe<T> {
    void subscribe(Observer<T> observer) throws Exception;
}