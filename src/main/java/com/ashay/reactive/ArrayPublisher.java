package com.ashay.reactive;

import java.util.Objects;

public class ArrayPublisher<T> implements Publisher<T> {
    private final T[] array;

    public ArrayPublisher(T[] array) {

        this.array = array;
    }

    @Override
    public void subscribe(Subscriber<T> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            int index = 0;
            long requested;
            boolean cancelled;

            @Override
            public void request(long number) {
                long initialRequested = requested;

                requested += number;

                if (initialRequested > 0) {
                    return;
                }

                long sent = 0;

                for (; sent < requested && index < array.length; sent++, index++) {
                    if (cancelled) {
                        return;
                    }
                    T t = array[index];
                    if (Objects.isNull(t)) {
                        subscriber.onError(new NullPointerException());
                        return;
                    }
                    subscriber.onNext(t);
                }

                if (cancelled) {
                    return;
                }

                if (index == array.length) {
                    subscriber.onComplete();
                    return;
                }

                requested -= sent;
            }

            @Override
            public void cancel() {
                cancelled = true;
            }
        });
    }
}
