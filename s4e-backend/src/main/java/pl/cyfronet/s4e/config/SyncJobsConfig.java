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

package pl.cyfronet.s4e.config;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.cyfronet.s4e.admin.sync.PrefixScanner;
import pl.cyfronet.s4e.admin.sync.task.ChunkedRunner;
import pl.cyfronet.s4e.admin.sync.task.SyncJobManager;
import pl.cyfronet.s4e.admin.sync.task.SyncJobStore;
import pl.cyfronet.s4e.properties.S3Properties;
import pl.cyfronet.s4e.properties.SyncJobsProperties;
import pl.cyfronet.s4e.sync.ContextRecorder;
import pl.cyfronet.s4e.sync.SceneAcceptor;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Clock;

@Configuration
public class SyncJobsConfig {
    @Autowired
    private SyncJobsProperties syncJobsProperties;

    @Autowired
    private S3Properties s3Properties;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private SceneAcceptor sceneAcceptor;

    @Autowired
    private ContextRecorder contextRecorder;

    @Autowired
    private Clock clock;

    @Bean
    public PrefixScanner prefixScanner() {
        String bucket = s3Properties.getBucket();
        return new PrefixScanner(bucket, s3Client);
    }

    @Bean
    public SyncJobStore syncJobStore() {
        return new SyncJobStore();
    }

    @Bean
    public ChunkedRunner chunkedRunner() {
        return new ChunkedRunner(syncJobsProperties.getThreadPoolSize() * 2, syncJobExecutor());
    }

    @Bean
    public SyncJobManager syncJobManager() {
        return new SyncJobManager(chunkedRunner(), prefixScanner(), sceneAcceptor, contextRecorder, clock);
    }

    @Bean
    public AsyncTaskExecutor syncJobExecutor() {
        val executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(syncJobsProperties.getThreadPoolSize());
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(1);
        executor.initialize();
        return executor;
    }
}
