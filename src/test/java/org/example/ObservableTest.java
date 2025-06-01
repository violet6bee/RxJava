package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ObservableTest {

    @Test
    public void testJustOperator() {
        List<Integer> results = new ArrayList<>();
        Observable.just(1, 2, 3).subscribe(new TestObserver<>(results));
        assertEquals(Arrays.asList(1, 2, 3), results);
    }

    @Test
    public void testMapOperator() {
        List<Integer> results = new ArrayList<>();
        Observable.just(1, 2, 3)
                .map(x -> x * 2)
                .subscribe(new TestObserver<>(results));
        assertEquals(Arrays.asList(2, 4, 6), results);
    }

    @Test
    public void testFilterOperator() {
        List<Integer> results = new ArrayList<>();
        Observable.just(1, 2, 3, 4, 5)
                .filter(x -> x % 2 == 0)
                .subscribe(new TestObserver<>(results));
        assertEquals(Arrays.asList(2, 4), results);
    }

    @Test
    public void testErrorHandling() {
        List<String> events = new ArrayList<>();
        Observable.just(1, 2, 0, 4)
                .map(x -> 10 / x)
                .subscribe(new Observer<Integer>() {
                    @Override public void onSubscribe(Disposable d) {}
                    @Override public void onNext(Integer item) { events.add("Next: " + item); }
                    @Override public void onError(Throwable e) { events.add("Error: " + e.getMessage()); }
                    @Override public void onComplete() { events.add("Complete"); }
                });

        assertTrue(events.contains("Error: / by zero"));
        assertFalse(events.contains("Complete"));
    }

    @Test
    public void testDisposable() {
        List<Integer> results = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicReference<Disposable> disposableRef = new AtomicReference<>();

        Observable.just(1, 2, 3)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableRef.set(d);
                    }

                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                        if (item == 2) {
                            disposableRef.get().dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        fail("Unexpected error");
                    }

                    @Override
                    public void onComplete() {
                        completed.set(true);
                    }
                });

        assertEquals(Arrays.asList(1, 2), results);
        assertTrue(disposableRef.get().isDisposed());
        assertFalse(completed.get());
    }

    private static class TestObserver<T> implements Observer<T> {
        private final List<T> results;

        TestObserver(List<T> results) {
            this.results = results;
        }

        @Override public void onSubscribe(Disposable d) {}
        @Override public void onNext(T item) { results.add(item); }
        @Override public void onError(Throwable e) { fail("Unexpected error: " + e.getMessage()); }
        @Override public void onComplete() {}
    }
}