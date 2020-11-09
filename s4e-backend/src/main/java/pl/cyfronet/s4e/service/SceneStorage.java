package pl.cyfronet.s4e.service;

import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.S3ClientException;
import pl.cyfronet.s4e.properties.S3Properties;
import pl.cyfronet.s4e.util.SceneArtifactsHelper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

@Service
public class SceneStorage extends Storage {
    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;
    private final SceneArtifactsHelper sceneArtifactsHelper;

    public SceneStorage(S3Client s3Client,
                        S3Properties s3Properties,
                        S3Presigner s3Presigner,
                        SceneArtifactsHelper sceneArtifactsHelper) {
        super(s3Client);
        this.s3Properties = s3Properties;
        this.s3Presigner = s3Presigner;
        this.sceneArtifactsHelper = sceneArtifactsHelper;
    }

    public String get(String key) throws NotFoundException, S3ClientException {
        return get(key, s3Properties.getBucket());
    }

    public boolean exists(String key) throws S3ClientException {
        return exists(key, s3Properties.getBucket());
    }

    public URL generatePresignedGetLink(Long id, Duration signatureDuration) throws NotFoundException {
        return generatePresignedGetLinkWithFileType(id, null, signatureDuration);
    }

    public URL generatePresignedGetLinkWithFileType(Long id, String artifactName, Duration signatureDuration) throws NotFoundException {
        String key = sceneArtifactsHelper.getArtifact(id, artifactName);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
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

    public Duration getPresignedGetTimeout() {
        return s3Properties.getPresignedGetTimeout();
    }
}
