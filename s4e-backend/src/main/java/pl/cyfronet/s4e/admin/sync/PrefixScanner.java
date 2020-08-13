package pl.cyfronet.s4e.admin.sync;

import lombok.RequiredArgsConstructor;
import lombok.val;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class PrefixScanner {
    private final String bucket;
    private final S3Client s3Client;

    public Stream<S3Object> scan(String prefix) {
        val request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();
        ListObjectsV2Iterable objectsIterable = s3Client.listObjectsV2Paginator(request);
        return objectsIterable.contents().stream();
    }
}
