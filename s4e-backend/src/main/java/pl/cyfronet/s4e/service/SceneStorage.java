package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.properties.S3Properties;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SceneStorage {
    interface SceneProjection {
        String getS3Path();
    }

    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final SceneRepository sceneRepository;

    public String get(String key) throws NotFoundException, S3ClientException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .build();

        try {
            return s3Client.getObjectAsBytes(getObjectRequest)
                    .asString(StandardCharsets.UTF_8);
        } catch (NoSuchKeyException e) {
            throw new NotFoundException(e);
        } catch (SdkException e) {
            throw new S3ClientException(e);
        }
    }

    public boolean exists(String key) throws S3ClientException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .build();
        try {
            s3Client.getObject(getObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (SdkException e) {
            throw new S3ClientException(e);
        }
    }

    public URL generatePresignedGetLink(Long id, Duration signatureDuration) throws NotFoundException {
        SceneProjection sceneProjection = sceneRepository.findById(id, SceneProjection.class)
                .orElseThrow(() -> new NotFoundException("Scene with id '" + id + "' not found"));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(sceneProjection.getS3Path())
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(signatureDuration)
                .build());

        if (!presignedGetObjectRequest.isBrowserExecutable()) {
            throw new IllegalStateException("The returned link must be a GET request without additional headers");
        }

        return presignedGetObjectRequest.url();
    }

    public Duration getPresignedGetTimeout() {
        return s3Properties.getPresignedGetTimeout();
    }
}
