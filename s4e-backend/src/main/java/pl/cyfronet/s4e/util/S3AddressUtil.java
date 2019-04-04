package pl.cyfronet.s4e.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3AddressUtil {
    @Value("${s3.geoserver.endpoint}")
    private String s3GeoserverEndpoint;

    @Value("${s3.geoserver.bucket}")
    private String s3GeoserverBucket;

    public String getS3Address(String path) {
        return s3GeoserverEndpoint+"://"+s3GeoserverBucket+"/"+path;
    }
}
