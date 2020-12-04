package pl.cyfronet.s4e.admin.sync.task;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.cyfronet.s4e.TestAwaitilityConfiguration;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class ChunkedRunnerTest {
    private ChunkedRunner chunkedRunner;
    private ThreadPoolTaskExecutor executor;
    private List<MockRunnable> jobs;

    @BeforeAll
    public static void beforeAll() {
        TestAwaitilityConfiguration.initialize();
    }

    @BeforeEach
    public void beforeEach() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.initialize();
        chunkedRunner = new ChunkedRunner(2, executor);

        jobs = List.of(
                new MockRunnable(),
                new MockRunnable(),
                new MockRunnable(),
                new MockRunnable(),
                new MockRunnable()
        );
    }

    @AfterEach
    public void afterEach() {
        executor.shutdown();
    }

    public static class MockRunnable implements Runnable {
        private final Semaphore semaphore = new Semaphore(0);
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final AtomicBoolean finished = new AtomicBoolean(false);

        private RuntimeException exception = null;

        @Override
        public void run() {
            try {
                running.set(true);

                semaphore.acquire();

                running.set(false);
                finished.set(true);

                if (exception != null) {
                    throw exception;
                }
            } catch (InterruptedException e) {
                fail("Interruption is not expected", e);
            }
        }
    }

    @Test
    public void happyPath() {
        Runnable afterCompletion = spy(Runnable.class);

        Future<?> future = chunkedRunner.run(jobs.iterator(), afterCompletion);

        // Wait for the two first jobs to be submitted and running.
        await().untilAtomic(jobs.get(0).running, is(true));
        await().untilAtomic(jobs.get(1).running, is(true));
        assertThat(future.isDone(), is(equalTo(false)));

        // Release the first job
        jobs.get(0).semaphore.release();

        // and ensure it's finished, and the third is submitted and running.
        await().untilTrue(jobs.get(0).finished);
        await().untilTrue(jobs.get(2).running);
        assertThat(future.isDone(), is(equalTo(false)));

        // Release all the other jobs
        for (int i = 1; i < 5; i++) {
            jobs.get(i).semaphore.release();
        }

        // and wait for the future to be done.
        await().until(future::isDone, is(true));
        // When it happens, all jobs should already be finished
        for (MockRunnable job : jobs) {
            assertThat(job.finished.get(), is(true));
        }
        // and the afterCompletion hook called.
        verify(afterCompletion).run();
    }

    @Test
    public void unexpectedException() {
        Runnable afterCompletion = spy(Runnable.class);

        Future<?> future = chunkedRunner.run(jobs.iterator(), afterCompletion);

        // Wait for the two first jobs to be submitted and running.
        await().untilAtomic(jobs.get(0).running, is(true));
        await().untilAtomic(jobs.get(1).running, is(true));
        assertThat(future.isDone(), is(equalTo(false)));

        // Release the first job
        jobs.get(0).semaphore.release();

        // and ensure it's finished, and the third is submitted and running.
        await().untilTrue(jobs.get(0).finished);
        await().untilTrue(jobs.get(2).running);
        assertThat(future.isDone(), is(equalTo(false)));

        // Set the second job to throw a RuntimeException
        jobs.get(1).exception = new RuntimeException();

        // and release all jobs.
        for (int i = 1; i < 5; i++) {
            jobs.get(i).semaphore.release();
        }

        // The output is the same as if nothing happened, however, info about exception is logged.
        // All the RE should be caught by the submitted Runnable.
        await().until(future::isDone, is(true));
        for (MockRunnable job : jobs) {
            assertThat(job.finished.get(), is(true));
        }
        verify(afterCompletion).run();
    }

    @Test
    public void cancellingJob() {
        Runnable afterCompletion = spy(Runnable.class);

        Future<?> future = chunkedRunner.run(jobs.iterator(), afterCompletion);

        // Wait for the two first jobs to be submitted and running.
        await().untilAtomic(jobs.get(0).running, is(true));
        await().untilAtomic(jobs.get(1).running, is(true));
        assertThat(future.isDone(), is(equalTo(false)));

        // Release the first job
        jobs.get(0).semaphore.release();

        // and ensure it's finished, and the third is submitted and running.
        await().untilTrue(jobs.get(0).finished);
        await().untilTrue(jobs.get(2).running);
        assertThat(future.isDone(), is(equalTo(false)));

        // Cancel the future.
        assertThat(future.cancel(true), is(true));

        // Release two enqueued jobs, last two shouldn't be enqueued after cancelling.
        for (int i = 1; i < 3; i++) {
            jobs.get(i).semaphore.release();
        }

        // Ensure the two running jobs manage to finish.
        await().untilTrue(jobs.get(1).finished);
        await().untilTrue(jobs.get(2).finished);
        // By then, the last two jobs shouldn't be enqueued,
        assertThat(jobs.get(3).running.get(), is(false));
        assertThat(jobs.get(4).running.get(), is(false));
        // and the completion callback shouldn't be called.
        verifyNoMoreInteractions(afterCompletion);
    }
}
