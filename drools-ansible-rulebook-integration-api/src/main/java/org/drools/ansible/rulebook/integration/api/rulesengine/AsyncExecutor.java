package org.drools.ansible.rulebook.integration.api.rulesengine;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncExecutor {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CompletableFuture<?> submit(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    public <T> CompletableFuture<T> submit(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
