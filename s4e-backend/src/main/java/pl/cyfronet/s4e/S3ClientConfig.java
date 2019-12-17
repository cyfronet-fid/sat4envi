package pl.cyfronet.s4e;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class S3ClientConfig {
    @Bean
    public S3Client s3Client(S3ClientProperties props) {
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
}
