package com.ashay.reactive;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;

public class ArrayPublisherTest {

    public String[] ranbow() {
        return new String[]{"violet", "indigo", "blue", "green", "yellow", "orange", "red"};
    }

    @Test
    void signalsInSequence() throws InterruptedException {
        Publisher<String> rainbowPublisher = new ArrayPublisher(ranbow());
        ArrayList<String> signals = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        rainbowPublisher.subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                signals.add("onSubscribe()");
                s.request(7);
            }

            @Override
            public void onNext(String s) {
                signals.add("onNext(" + s + ")");
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                signals.add("onComplete()");
                latch.countDown();
            }
        });

        assertThat(latch.await(1L, TimeUnit.SECONDS), is(true));
        assertThat(signals, contains(
                "onSubscribe()",
                "onNext(violet)",
                "onNext(indigo)",
                "onNext(blue)",
                "onNext(green)",
                "onNext(yellow)",
                "onNext(orange)",
                "onNext(red)",
                "onComplete()"
        ));
    }

    @Test
    void backpressure() throws InterruptedException {
        Publisher<String> rainbowPublisher = new ArrayPublisher<>(ranbow());
        ArrayList<String> collector = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Subscription[] subscriptions = new Subscription[1];

        rainbowPublisher.subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                subscriptions[0] = s;
            }

            @Override
            public void onNext(String s) {
                collector.add(s);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        subscriptions[0].request(1);
        assertThat(collector, hasSize(1));
        assertThat(collector, contains("violet"));

        subscriptions[0].request(1);
        assertThat(collector, hasSize(2));
        assertThat(collector, contains("violet", "indigo"));

        subscriptions[0].request(10);
        assertThat(collector, hasSize(7));
        assertThat(collector, contains("violet", "indigo", "blue", "green", "yellow", "orange", "red"));

        assertThat(latch.await(1L, TimeUnit.SECONDS), is(true));
    }

    @Test
    void error() throws InterruptedException {
        Publisher<String> rainbowPublisher = new ArrayPublisher<>(new String[]{"violet", "indigo", null, "green", "yellow", "orange", "red"});
        ArrayList<String> collector = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Subscription[] subscriptions = new Subscription[1];
        AtomicReference<Throwable> error = new AtomicReference<>();

        rainbowPublisher.subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                subscriptions[0] = s;
            }

            @Override
            public void onNext(String s) {
                collector.add(s);
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
                latch.countDown();
            }

            @Override
            public void onComplete() {
            }
        });

        subscriptions[0].request(10);
        assertThat(collector, hasSize(2));
        assertThat(collector, contains("violet", "indigo"));
        assertThat(error.get(), isA(NullPointerException.class));

        assertThat(latch.await(1L, TimeUnit.SECONDS), is(true));
    }
}
