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

package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.properties.FileStorageProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

@Service
@Profile({"!test", "integration"})
@Slf4j
public class FileStorage extends Storage {
    private final FileStorageProperties fileStorageProperties;

    public FileStorage(S3Client s3Client, FileStorageProperties fileStorageProperties) {
        super(s3Client);
        this.fileStorageProperties = fileStorageProperties;
    }

    public void upload(@NonNull String key, @NonNull byte[] payload) {
        String contentType;
        try {
            contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(payload));

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(fileStorageProperties.getBucket())
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(payload));
        } catch (IOException e) {
            throw new IllegalStateException("Shouldn't have happened", e);
        }
    }

    public void delete(@NonNull String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(fileStorageProperties.getBucket())
                        .key(key)
                        .build());
    }

    public boolean exists(String key) throws S3ClientException {
        return exists(key, fileStorageProperties.getBucket());
    }
}
