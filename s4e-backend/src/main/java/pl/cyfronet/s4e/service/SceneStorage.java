package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.SceneStorageProperties;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SceneStorage {
    interface SceneProjection {
        String getS3Path();
    }

    private final SceneStorageProperties sceneStorageProperties;
    private final S3Presigner s3Presigner;
    private final SceneRepository sceneRepository;

    public URL generatePresignedGetLink(Long id, Duration signatureDuration) throws NotFoundException {
        SceneProjection sceneProjection = sceneRepository.findById(id, SceneProjection.class)
                .orElseThrow(() -> new NotFoundException("Scene with id '" + id + "' not found"));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(sceneStorageProperties.getBucket())
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
        return sceneStorageProperties.getPresignedGetTimeout();
    }
}
