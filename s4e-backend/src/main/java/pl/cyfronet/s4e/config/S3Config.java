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

package pl.cyfronet.s4e.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.cyfronet.s4e.properties.S3Properties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Profile({"!test", "integration"})
public class S3Config {
    @Bean
    public S3Client s3Client(S3Properties props) {
        AwsCredentials credentials = AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());
        return S3Client.builder()
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        // Minio returns checksums with trailing '-1', which causes some requests to fail regardless of
                        // actual result. See https://github.com/minio/minio/issues/8620.
                        .checksumValidationEnabled(false)
                        .build())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(props.getEndpoint())
                // Required by the AWS API, but not used with overridden endpoint.
                .region(Region.US_WEST_1)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties props) {
        AwsCredentials credentials = AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());
        return S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(props.getEndpoint())
                // Required by the AWS API, but not used with overridden endpoint.
                .region(Region.US_WEST_1)
                .build();
    }
}
