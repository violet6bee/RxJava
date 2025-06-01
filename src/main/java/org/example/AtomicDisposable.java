package org.example;

public class AtomicDisposable implements Disposable {
    private volatile boolean disposed = false;
    private Disposable wrapped;

    @Override
    public void dispose() {
        disposed = true;
        if (wrapped != null) {
            wrapped.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed || (wrapped != null && wrapped.isDisposed());
    }

    public void add(Disposable disposable) {
        if (disposed) {
            disposable.dispose();
        } else {
            wrapped = disposable;
        }
    }
}