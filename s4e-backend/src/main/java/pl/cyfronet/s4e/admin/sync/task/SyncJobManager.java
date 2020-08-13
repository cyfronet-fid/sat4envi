package pl.cyfronet.s4e.admin.sync.task;

import lombok.RequiredArgsConstructor;
import pl.cyfronet.s4e.admin.sync.PrefixScanner;
import pl.cyfronet.s4e.sync.Error;
import pl.cyfronet.s4e.sync.SceneAcceptor;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SyncJobManager {
    private final ChunkedRunner chunkedRunner;
    private final PrefixScanner prefixScanner;
    private final SceneAcceptor sceneAcceptor;
    private final Clock clock;

    public SyncJob create(String name, String prefix, boolean failFast) {
        SyncJob task = new SyncJob(name, prefix, failFast);

        SyncJob.PendingDetails pendingMetric = new SyncJob.PendingDetails(LocalDateTime.now(clock));
        task.addState(pendingMetric);

        long sceneCount = sceneKeyStream(prefix).count();
        pendingMetric.setSceneCountElapsed(elapsedSince(pendingMetric.getSince()));
        pendingMetric.setSceneCount(sceneCount);

        return task;
    }

    public void run(SyncJob syncJob) {
        synchronized (syncJob) {
            if (syncJob.getState() != SyncJob.State.PENDING) {
                throw new IllegalArgumentException("You can only run a pending job");
            }

            syncJob.setState(SyncJob.State.RUNNING);
            SyncJob.RunningDetails runningMetric = new SyncJob.RunningDetails(LocalDateTime.now(clock));
            syncJob.addState(runningMetric);

            scheduleTask(syncJob, runningMetric);
        }
    }

    public void cancel(SyncJob syncJob) {
        synchronized (syncJob) {
            doCancel(syncJob);
        }
    }

    private void doCancel(SyncJob syncJob) {
        if (syncJob.getState() != SyncJob.State.RUNNING) {
            throw new IllegalArgumentException("You can only cancel a running job");
        }

        SyncJob.StateDetails stateDetails = syncJob.getDetails(SyncJob.State.RUNNING);
        if (stateDetails instanceof SyncJob.RunningDetails) {
            ((SyncJob.RunningDetails) stateDetails).getFuture().cancel(true);
        }

        toElapsedState(syncJob, SyncJob.State.CANCELLED);
    }

    private void scheduleTask(SyncJob syncJob, SyncJob.RunningDetails runningMetric) {
        Iterator<Runnable> iterator = sceneKeyStream(syncJob.getPrefix())
                .map(sceneKey -> (Runnable) () -> {
                    synchronized (syncJob) {
                        runningMetric.setRunningCount(runningMetric.getRunningCount() + 1);
                    }
                    Error error = sceneAcceptor.accept(sceneKey);
                    if (syncJob.isFailFast() && error != null) {
                        synchronized (syncJob) {
                            boolean jobCancelledAlready = syncJob.getState() != SyncJob.State.RUNNING;
                            if (!jobCancelledAlready) {
                                doCancel(syncJob);
                            }
                        }
                    }
                    synchronized (syncJob) {
                        runningMetric.setRunningCount(runningMetric.getRunningCount() - 1);
                        if (error != null) {
                            runningMetric.addError(error);
                        } else {
                            runningMetric.setSuccessesCount(runningMetric.getSuccessesCount() + 1);
                        }
                    }
                })
                .iterator();
        Future<?> future = chunkedRunner.run(iterator, () -> {
            synchronized (syncJob) {
                toElapsedState(syncJob, SyncJob.State.FINISHED);
            }
        });
        runningMetric.setFuture(future);
    }

    private void toElapsedState(SyncJob syncJob, SyncJob.State state) {
        if (!Set.of(SyncJob.State.CANCELLED, SyncJob.State.FINISHED).contains(state)) {
            throw new IllegalArgumentException("Only CANCELLED and FINISHED status can be set");
        }
        syncJob.setState(state);
        Duration elapsed = elapsedSince(syncJob.getDetails(SyncJob.State.RUNNING).getSince());
        syncJob.addState(new SyncJob.ElapsedDetails(syncJob.getState(), LocalDateTime.now(clock), elapsed));
    }

    private Stream<String> sceneKeyStream(String prefix) {
        return prefixScanner.scan(prefix)
                .map(S3Object::key)
                .filter(key -> key.endsWith(".scene"));
    }

    private Duration elapsedSince(Temporal temporal) {
        return Duration.between(temporal, LocalDateTime.now(clock));
    }
}
