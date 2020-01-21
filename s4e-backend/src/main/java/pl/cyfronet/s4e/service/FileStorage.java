package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
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
@RequiredArgsConstructor
@Slf4j
public class FileStorage {
    private final S3Client s3Client;
    private final FileStorageProperties fileStorageProperties;

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
}
