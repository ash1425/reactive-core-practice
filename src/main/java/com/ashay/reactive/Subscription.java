package com.ashay.reactive;

public interface Subscription {

    void request(long number);

    void cancel();
}
