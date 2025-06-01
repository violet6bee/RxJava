package org.example;

import java.util.HashSet;
import java.util.Set;

public class CompositeDisposable implements Disposable {
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