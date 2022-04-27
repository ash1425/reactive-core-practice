package com.ashay.reactive;

import java.util.Objects;

public class ArrayPublisher<T> implements Publisher<T> {
    private final T[] ranbow;

    public ArrayPublisher(T[] ranbow) {

        this.ranbow = ranbow;
    }

    @Override
    public void subscribe(Subscriber<T> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            int index = 0;

            @Override
            public void request(long number) {
                for (int i = 0; i < number && index < ranbow.length; i++, index++) {
                    T t = ranbow[index];
                    if (Objects.isNull(t)) {
                        subscriber.onError(new NullPointerException());
                        return;
                    }
                    subscriber.onNext(t);
                }
            }

            @Override
            public void cancel() {

            }
        });
        subscriber.onComplete();
    }
}
