package com.ashay.reactive;

public interface Publisher<T> {

    void subscribe(Subscriber<T> subscriber);
}
