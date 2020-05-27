package pl.cyfronet.s4e.service;

import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.S3ClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.nio.charset.StandardCharsets;

public class Storage {
    final S3Client s3Client;

    public Storage(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public boolean exists(String key, String bucket) throws S3ClientException {
        verifyKey(key);
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        try {
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (SdkException e) {
            throw new S3ClientException(e);
        }
    }

    public String get(String key, String bucket) throws NotFoundException, S3ClientException {
        verifyKey(key);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
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

    protected static void verifyKey(String key) {
        if (key.startsWith("/")) {
            throw new IllegalArgumentException("Key must not have a leading slash");
        }
    }
}
