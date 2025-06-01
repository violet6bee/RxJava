package org.example;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;


public class Observable<T> {
    private final ObservableOnSubscribe<T> source;

    private Observable(ObservableOnSubscribe<T> source) {
        this.source = source;
    }

    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return new Observable<>(source);
    }

    public static <T> Observable<T> just(T... items) {
        return create(emitter -> {
            Disposable disposable = new DefaultDisposable();
            emitter.onSubscribe(disposable);

            try {
                for (T item : items) {
                    if (disposable.isDisposed()) {
                        return;
                    }
                    emitter.onNext(item);
                }
                if (!disposable.isDisposed()) {
                    emitter.onComplete();
                }
            } catch (Exception e) {
                if (!disposable.isDisposed()) {
                    emitter.onError(e);
                }
            }
        });
    }

    public Disposable subscribe(Observer<? super T> observer) {
        AtomicDisposable disposable = new AtomicDisposable();

        try {
            source.subscribe(new Observer<T>() {
                @Override
                public void onSubscribe(Disposable d) {
                    disposable.add(d);
                    observer.onSubscribe(disposable);
                }

                @Override
                public void onNext(T item) {
                    if (!disposable.isDisposed()) {
                        observer.onNext(item);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (!disposable.isDisposed()) {
                        disposable.dispose();
                        observer.onError(throwable);
                    }
                }

                @Override
                public void onComplete() {
                    if (!disposable.isDisposed()) {
                        disposable.dispose();
                        observer.onComplete();
                    }
                }
            });
        } catch (Exception e) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
                observer.onError(e);
            }
        }

        return disposable;
    }

    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return create(emitter -> {
            subscribe(new Observer<T>() {
                @Override
                public void onSubscribe(Disposable d) {
                    emitter.onSubscribe(d);
                }

                @Override
                public void onNext(T item) {
                    try {
                        emitter.onNext(mapper.apply(item));
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    emitter.onError(throwable);
                }

                @Override
                public void onComplete() {
                    emitter.onComplete();
                }
            });
        });
    }

    public Observable<T> filter(Predicate<? super T> predicate) {
        return create(emitter -> {
            subscribe(new Observer<T>() {
                @Override
                public void onSubscribe(Disposable d) {
                    emitter.onSubscribe(d);
                }

                @Override
                public void onNext(T item) {
                    try {
                        if (predicate.test(item)) {
                            emitter.onNext(item);
                        }
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    emitter.onError(throwable);
                }

                @Override
                public void onComplete() {
                    emitter.onComplete();
                }
            });
        });
    }

    public <R> Observable<R> flatMap(Function<? super T, ? extends Observable<? extends R>> mapper) {
        return create(emitter -> {
            CompositeDisposable composite = new CompositeDisposable();
            emitter.onSubscribe(composite);

            subscribe(new Observer<T>() {
                @Override
                public void onSubscribe(Disposable d) {
                    composite.add(d);
                }

                @Override
                public void onNext(T item) {
                    if (composite.isDisposed()) {
                        return;
                    }

                    try {
                        Observable<? extends R> observable = mapper.apply(item);
                        Disposable disposable = observable.subscribe(
                                new Observer<R>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        composite.add(d);
                                    }

                                    @Override
                                    public void onNext(R rItem) {
                                        emitter.onNext(rItem);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        emitter.onError(throwable);
                                    }

                                    @Override
                                    public void onComplete() {
                                        // Не вызываем внешний onComplete
                                    }
                                }
                        );
                        composite.add(disposable);
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    emitter.onError(throwable);
                }

                @Override
                public void onComplete() {
                    emitter.onComplete();
                }
            });
        });
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return create(emitter -> {
            DefaultDisposable disposable = new DefaultDisposable();
            emitter.onSubscribe(disposable);

            scheduler.schedule(() -> {
                if (!disposable.isDisposed()) {
                    subscribe(emitter);
                }
            });
        });
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return create(emitter -> {
            DefaultDisposable disposable = new DefaultDisposable();
            emitter.onSubscribe(disposable);

            subscribe(new Observer<T>() {
                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onNext(T item) {
                    scheduler.schedule(() -> {
                        if (!disposable.isDisposed()) {
                            emitter.onNext(item);
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    scheduler.schedule(() -> {
                        if (!disposable.isDisposed()) {
                            emitter.onError(throwable);
                        }
                    });
                }

                @Override
                public void onComplete() {
                    scheduler.schedule(() -> {
                        if (!disposable.isDisposed()) {
                            emitter.onComplete();
                        }
                    });
                }
            });
        });
    }

    private static class DefaultDisposable implements Disposable {
        private volatile boolean disposed = false;

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }

    private static class CompositeDisposable implements Disposable {
        private final Set<Disposable> disposables = new HashSet<>();
        private volatile boolean disposed = false;

        public void add(Disposable disposable) {
            if (!disposed) {
                synchronized (this) {
                    if (!disposed) {
                        disposables.add(disposable);
                        return;
                    }
                }
            }
            disposable.dispose();
        }

        @Override
        public void dispose() {
            if (!disposed) {
                synchronized (this) {
                    if (disposed) {
                        return;
                    }
                    disposed = true;
                    disposables.forEach(Disposable::dispose);
                    disposables.clear();
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}