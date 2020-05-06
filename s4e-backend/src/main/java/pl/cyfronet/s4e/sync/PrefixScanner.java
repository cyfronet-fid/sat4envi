package pl.cyfronet.s4e.sync;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.properties.S3Properties;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class PrefixScanner {
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public Stream<S3Object> scan(String prefix) {
        String bucket =  s3Properties.getBucket();
        val request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();
        ListObjectsV2Iterable objectsIterable = s3Client.listObjectsV2Paginator(request);
        return objectsIterable.contents().stream();
    }
}
