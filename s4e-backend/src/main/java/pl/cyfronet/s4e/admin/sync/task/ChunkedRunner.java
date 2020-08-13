package pl.cyfronet.s4e.admin.sync.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@RequiredArgsConstructor
@Slf4j
public class ChunkedRunner {
    private final int chunkSize;
    private final AsyncTaskExecutor syncJobExecutor;

    public Future<?> run(Iterator<? extends Runnable> iterator, Runnable afterCompletion) {
        Runnable runnable = () -> {
            Semaphore chunkSemaphore = new Semaphore(chunkSize);
            List<Future<?>> futures = new ArrayList<>(chunkSize);

            try {
                while (iterator.hasNext()) {
                    chunkSemaphore.acquire();
                    futures.removeIf(Future::isDone);

                    Runnable next = iterator.next();
                    Future<?> future = syncJobExecutor.submit(() -> {
                        try {
                            next.run();
                        } catch (RuntimeException e) {
                            log.warn("Unexpected RuntimeException", e);
                        } finally {
                            chunkSemaphore.release();
                        }
                    });
                    futures.add(future);
                }

                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (ExecutionException e) {
                        log.info("Exception when waiting for residual tasks", e);
                    }
                }
                afterCompletion.run();
            } catch (InterruptedException e) {
                // Don't submit new tasks but allow running ones to finish.
                Thread.currentThread().interrupt();
            }
        };

        return syncJobExecutor.submit(runnable);
    }
}
