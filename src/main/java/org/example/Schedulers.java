package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Schedulers {
    public static Scheduler io() {
        return new IoScheduler();
    }

    public static Scheduler computation() {
        return new ComputationScheduler();
    }

    public static Scheduler single() {
        return new SingleThreadScheduler();
    }

    private static class IoScheduler implements Scheduler {
        private final ExecutorService executor = Executors.newCachedThreadPool();

        @Override
        public void schedule(Runnable task) {
            executor.execute(task);
        }

        @Override
        public void shutdown() {
            executor.shutdown();
        }
    }

    private static class ComputationScheduler implements Scheduler {
        private final ExecutorService executor =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        @Override
        public void schedule(Runnable task) {
            executor.execute(task);
        }

        @Override
        public void shutdown() {
            executor.shutdown();
        }
    }

    private static class SingleThreadScheduler implements Scheduler {
        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        @Override
        public void schedule(Runnable task) {
            executor.execute(task);
        }

        @Override
        public void shutdown() {
            executor.shutdown();
        }
    }
}