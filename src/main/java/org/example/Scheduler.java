package org.example;

public interface Scheduler {
    void schedule(Runnable task);
    void shutdown();
}