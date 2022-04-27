package com.ashay.reactive;

public interface Subscriber<T> {

    void onSubscribe(Subscription s);

    void onNext(T t);

    void onError(Throwable t);

    void onComplete();
}
