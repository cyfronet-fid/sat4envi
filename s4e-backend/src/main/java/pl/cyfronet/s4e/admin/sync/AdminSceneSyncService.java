package pl.cyfronet.s4e.admin.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.properties.SeedProperties;
import pl.cyfronet.s4e.sync.PrefixScanner;
import pl.cyfronet.s4e.sync.SceneAcceptor;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSceneSyncService {
    private final PrefixScanner prefixScanner;
    private final SceneAcceptor sceneAcceptor;
    private final SeedProperties seedProperties;

    public void readScenes(String prefix) {
        // AWS SDK has a default connection pool size of 50, so 40 threads should be fine.
        ExecutorService es = Executors.newFixedThreadPool(40);

        try {
            List<String> allSceneKeys = prefixScanner.scan(prefix)
                    .map(S3Object::key)
                    .filter(key -> key.endsWith(".scene"))
                    .collect(Collectors.toList());
            log.info(String.format("Scanning prefix: '%s'. Total: %d", prefix, allSceneKeys.size()));
            List<String> sceneKeysToSync = optionallyTruncateToLimit(allSceneKeys, seedProperties.getS4eSyncV1().getLimit());
            AtomicInteger count = new AtomicInteger(0);
            List<? extends Future<?>> futures = sceneKeysToSync.stream()
                    .map(sceneKey -> (Runnable) () -> {
                        sceneAcceptor.accept(sceneKey);
                        int i = count.addAndGet(1);
                        log.info(String.format("%d/%d. scene key: '%s'", i, sceneKeysToSync.size(), sceneKey));
                    })
                    .map(es::submit)
                    .collect(Collectors.toList());
            for (Future future : futures) {
                future.get();
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            log.error(e.getMessage(), e);
        } finally {
            es.shutdown();
        }
    }

    private <T> List<T> optionallyTruncateToLimit(List<T> list, int limit) {
        if (limit > 0) {
            return list.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } else {
            return list;
        }
    }
}
