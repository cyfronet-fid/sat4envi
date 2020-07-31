package pl.cyfronet.s4e.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.properties.S3Properties;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

import static pl.cyfronet.s4e.bean.Schema.SCENE_SCHEMA_ARTIFACTS_KEY;

@Service
public class SceneStorage extends Storage {
    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;
    private final SceneRepository sceneRepository;

    interface SceneProjection {
        String getS3Path();

        JsonNode getSceneContent();
    }

    public SceneStorage(S3Client s3Client,
                        S3Properties s3Properties,
                        S3Presigner s3Presigner,
                        SceneRepository sceneRepository) {
        super(s3Client);
        this.s3Properties = s3Properties;
        this.s3Presigner = s3Presigner;
        this.sceneRepository = sceneRepository;
    }

    public String get(String key) throws NotFoundException, S3ClientException {
        return get(key, s3Properties.getBucket());
    }

    public boolean exists(String key) throws S3ClientException {
        return exists(key, s3Properties.getBucket());
    }

    public URL generatePresignedGetLink(Long id, Duration signatureDuration) throws NotFoundException {
        return generatePresignedGetLinkWithFileType(id, signatureDuration, null);
    }

    public URL generatePresignedGetLinkWithFileType(Long id, Duration signatureDuration, String type) throws NotFoundException {
        SceneProjection sceneProjection = sceneRepository.findById(id, SceneProjection.class)
                .orElseThrow(() -> new NotFoundException("Scene with id '" + id + "' not found"));
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(getKey(sceneProjection, type))
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest =
                s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .getObjectRequest(getObjectRequest)
                        .signatureDuration(signatureDuration)
                        .build());

        if (!presignedGetObjectRequest.isBrowserExecutable()) {
            throw new IllegalStateException("The returned link must be a GET request without additional headers");
        }

        return presignedGetObjectRequest.url();
    }

    public String getKey(SceneProjection sceneProjection, String type) {
        if (type != null) {
            return sceneProjection.getSceneContent().get(SCENE_SCHEMA_ARTIFACTS_KEY).get(type).asText().substring(1);
        }
        return sceneProjection.getS3Path();
    }

    public Duration getPresignedGetTimeout() {
        return s3Properties.getPresignedGetTimeout();
    }
}
