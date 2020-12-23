/*
 * Copyright 2020 ACC Cyfronet AGH
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

package pl.cyfronet.s4e;

import org.springframework.context.annotation.Bean;
import pl.cyfronet.s4e.service.FileStorage;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.mockito.Mockito.mock;

public class TestMockConfiguration {
    @Bean
    public FileStorage fileStorage() {
        return mock(FileStorage.class);
    }

    @Bean
    public S3Client s3Client() {
        return mock(S3Client.class);
    }

    @Bean
    public S3Presigner s3Presigner() {
        return mock(S3Presigner.class);
    }
}
