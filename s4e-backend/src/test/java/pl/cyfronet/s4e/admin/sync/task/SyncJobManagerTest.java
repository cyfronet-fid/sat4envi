/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.admin.sync.task;

import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.cyfronet.s4e.TestAwaitilityConfiguration;
import pl.cyfronet.s4e.TestClock;
import pl.cyfronet.s4e.admin.sync.PrefixScanner;
import pl.cyfronet.s4e.sync.ContextRecorder;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.SceneAcceptor;
import pl.cyfronet.s4e.sync.context.Context;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class SyncJobManagerTest {
    private ThreadPoolTaskExecutor executor;
    private PrefixScanner prefixScanner;
    private SceneAcceptor sceneAcceptor;
    private ContextRecorder contextRecorder;
    private TestClock clock;
    private SyncJobManager syncJobManager;

    @BeforeAll
    public static void beforeAll() {
        TestAwaitilityConfiguration.initialize();
    }

    @BeforeEach
    public void beforeEach() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.initialize();
        ChunkedRunner chunkedRunner = new ChunkedRunner(2, executor);
        prefixScanner = mock(PrefixScanner.class);
        sceneAcceptor = mock(SceneAcceptor.class);
        contextRecorder = mock(ContextRecorder.class);
        clock = new TestClock(LocalDateTime.now());
        syncJobManager =  new SyncJobManager(chunkedRunner, prefixScanner, sceneAcceptor, contextRecorder, clock);
    }

    @AfterEach
    public void afterEach() {
        executor.shutdown();
    }

    @Test
    public void runSyncJob() {
        // 1. Create SyncJob.
        SyncJob syncJob = createSyncJob(false, () -> Stream.of("a", "b", "c"));

        // 2. Run it.
        Semaphore semaphoreA = new Semaphore(0);
        when(sceneAcceptor.accept(new Context("a.scene"))).thenAnswer(invocationOnMock -> {
            semaphoreA.acquire();
            return null;
        });
        when(sceneAcceptor.accept(new Context("b.scene"))).thenReturn(mock(Error.class));

        clock.forward(Durations.ONE_SECOND);

        syncJobManager.run(syncJob);

        assertThat(syncJob.getState(), is(equalTo(SyncJob.State.RUNNING)));
        SyncJob.RunningDetails runningDetails = (SyncJob.RunningDetails) syncJob.getDetails(SyncJob.State.RUNNING);
        assertThat(runningDetails.getSince(), is(equalTo(LocalDateTime.now(clock))));

        await().until(runningDetails::getSuccessesCount, is(equalTo(1L)));
        await().until(runningDetails::getErrorsCount, is(equalTo(1L)));
        assertThat(syncJob.getState(), is(equalTo(SyncJob.State.RUNNING)));

        // 3. Allow it to finish.
        clock.forward(Durations.ONE_SECOND);
        semaphoreA.release();

        await().until(syncJob::getState, is(equalTo(SyncJob.State.FINISHED)));

        assertThat(runningDetails.getSuccessesCount(), is(equalTo(2L)));
        assertThat(runningDetails.getErrorsCount(), is(equalTo(1L)));

        SyncJob.ElapsedDetails finishedDetails = (SyncJob.ElapsedDetails) syncJob.getDetails(SyncJob.State.FINISHED);
        assertThat(finishedDetails.getElapsed(), is(equalTo(Durations.ONE_SECOND)));
        assertThat(finishedDetails.getSince(), is(equalTo(LocalDateTime.now(clock))));
    }

    @Test
    public void runSyncJobAndFailFast() {
        // 1. Create SyncJob.
        SyncJob syncJob = createSyncJob(true, () -> Stream.of("a", "b", "c"));

        // 2. Run it.
        Semaphore semaphore1 = new Semaphore(0);
        when(sceneAcceptor.accept(new Context("a.scene"))).thenAnswer(invocationOnMock -> {
            semaphore1.acquire();
            return null;
        });
        Semaphore semaphore2 = new Semaphore(0);
        when(sceneAcceptor.accept(new Context("b.scene"))).thenAnswer(invocationOnMock -> {
            semaphore2.acquire();
            return mock(Error.class);
        });

        clock.forward(Durations.ONE_SECOND);

        syncJobManager.run(syncJob);

        assertThat(syncJob.getState(), is(equalTo(SyncJob.State.RUNNING)));
        SyncJob.RunningDetails runningDetails = (SyncJob.RunningDetails) syncJob.getDetails(SyncJob.State.RUNNING);
        assertThat(runningDetails.getSince(), is(equalTo(LocalDateTime.now(clock))));

        // 3. Make the second job fail.
        clock.forward(Durations.ONE_SECOND);
        semaphore2.release();

        await().until(runningDetails::getErrorsCount, is(equalTo(1L)));
        await().until(syncJob::getState, is(equalTo(SyncJob.State.CANCELLED)));
        assertThat(runningDetails.getSuccessesCount(), is(equalTo(0L)));

        SyncJob.ElapsedDetails cancelledDetails = (SyncJob.ElapsedDetails) syncJob.getDetails(SyncJob.State.CANCELLED);
        assertThat(cancelledDetails.getElapsed(), is(equalTo(Durations.ONE_SECOND)));
        assertThat(cancelledDetails.getSince(), is(equalTo(LocalDateTime.now(clock))));

        // 4. Allow the first job to execute.
        semaphore1.release();

        await().until(runningDetails::getSuccessesCount, is(equalTo(1L)));

        // 5. Ensure the third job is not executed.
        verify(sceneAcceptor, never()).accept(new Context("c.scene"));
    }

    @Test
    public void runSyncJobAndCancel() {
        // 1. Create SyncJob.
        SyncJob syncJob = createSyncJob(false, () -> Stream.of("a", "b", "c", "d"));

        // 2. Run it.
        Semaphore semaphoreA = new Semaphore(0);
        Semaphore semaphoreC = new Semaphore(0);
        when(sceneAcceptor.accept(new Context("a.scene"))).thenAnswer(invocationOnMock -> {
            semaphoreA.acquire();
            return null;
        });
        when(sceneAcceptor.accept(new Context("b.scene"))).thenReturn(mock(Error.class));
        when(sceneAcceptor.accept(new Context("c.scene"))).thenAnswer(invocationOnMock -> {
            semaphoreC.acquire();
            return null;
        });

        clock.forward(Durations.ONE_SECOND);

        syncJobManager.run(syncJob);

        assertThat(syncJob.getState(), is(equalTo(SyncJob.State.RUNNING)));
        SyncJob.RunningDetails runningDetails = (SyncJob.RunningDetails) syncJob.getDetails(SyncJob.State.RUNNING);
        assertThat(runningDetails.getSince(), is(equalTo(LocalDateTime.now(clock))));

        // 3. Cancel the job.
        await().until(runningDetails::getErrorsCount, is(equalTo(1L)));
        await().until(runningDetails::getRunningCount, is(equalTo(2L)));
        clock.forward(Durations.ONE_SECOND);

        syncJobManager.cancel(syncJob);

        assertThat(runningDetails.getSuccessesCount(), is(equalTo(0L)));
        assertThat(syncJob.getState(), is(equalTo(SyncJob.State.CANCELLED)));

        SyncJob.ElapsedDetails cancelledDetails = (SyncJob.ElapsedDetails) syncJob.getDetails(SyncJob.State.CANCELLED);
        assertThat(cancelledDetails.getElapsed(), is(equalTo(Durations.ONE_SECOND)));
        assertThat(cancelledDetails.getSince(), is(equalTo(LocalDateTime.now(clock))));

        // 4. Allow the two other jobs to finish.
        semaphoreA.release();
        semaphoreC.release();
        await().until(runningDetails::getSuccessesCount, is(equalTo(2L)));

        // 5. Ensure the fourth job is not executed.
        verify(sceneAcceptor, never()).accept(new Context("d.scene"));
    }

    @Test
    public void runSyncJobWithFailFastButCancelAndThenEmitError() {
        // 1. Create SyncJob.
        SyncJob syncJob = createSyncJob(true, () -> Stream.of("a", "b", "c"));

        // 2. Run it.
        Semaphore semaphoreA = new Semaphore(0);
        Semaphore semaphoreB = new Semaphore(0);
        when(sceneAcceptor.accept(new Context("a.scene"))).thenAnswer(invocationOnMock -> {
            semaphoreA.acquire();
            return null;
        });
        when(sceneAcceptor.accept(new Context("b.scene"))).thenAnswer(invocationOnMock -> {
            semaphoreB.acquire();
            return mock(Error.class);
        });

        clock.forward(Durations.ONE_SECOND);

        syncJobManager.run(syncJob);

        assertThat(syncJob.getState(), is(equalTo(SyncJob.State.RUNNING)));
        SyncJob.RunningDetails runningDetails = (SyncJob.RunningDetails) syncJob.getDetails(SyncJob.State.RUNNING);
        assertThat(runningDetails.getSince(), is(equalTo(LocalDateTime.now(clock))));

        // 3. Cancel the job.
        await().until(runningDetails::getRunningCount, is(equalTo(2L)));
        clock.forward(Durations.ONE_SECOND);

        syncJobManager.cancel(syncJob);

        assertThat(runningDetails.getSuccessesCount(), is(equalTo(0L)));
        assertThat(syncJob.getState(), is(equalTo(SyncJob.State.CANCELLED)));

        SyncJob.ElapsedDetails cancelledDetails = (SyncJob.ElapsedDetails) syncJob.getDetails(SyncJob.State.CANCELLED);
        assertThat(cancelledDetails.getElapsed(), is(equalTo(Durations.ONE_SECOND)));
        assertThat(cancelledDetails.getSince(), is(equalTo(LocalDateTime.now(clock))));

        // 4. Allow the two jobs to finish.
        semaphoreA.release();
        semaphoreB.release();
        await().until(runningDetails::getSuccessesCount, is(equalTo(1L)));
        await().until(runningDetails::getErrorsCount, is(equalTo(1L)));

        // 5. Ensure the third job is not executed.
        verify(sceneAcceptor, never()).accept(new Context("c.scene"));
    }

    private SyncJob createSyncJob(boolean failFast, Supplier<Stream<String>> keysPrefixSupplier) {
        when(prefixScanner.scan("prefix"))
                .thenAnswer(invocationOnMock -> keysPrefixSupplier.get().map(s -> s + ".scene").map(this::s3ObjectWithKey));

        SyncJob syncJob = syncJobManager.create("some-id", "prefix", failFast);

        assertThat(syncJob.getState(), is(equalTo(SyncJob.State.PENDING)));
        SyncJob.PendingDetails pendingDetails = (SyncJob.PendingDetails) syncJob.getDetails(SyncJob.State.PENDING);
        assertThat(pendingDetails.getSince(), is(equalTo(LocalDateTime.now(clock))));
        assertThat(pendingDetails.getSceneCount(), is(keysPrefixSupplier.get().count()));
        assertThat(pendingDetails.getSceneCountElapsed(), is(Duration.ZERO));
        return syncJob;
    }

    private S3Object s3ObjectWithKey(String key) {
        return S3Object.builder()
                .key(key)
                .build();
    }
}
