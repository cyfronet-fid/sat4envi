package pl.cyfronet.s4e.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.properties.GeoServerProperties;
import pl.cyfronet.s4e.properties.S3Properties;

@Service
@RequiredArgsConstructor
public class S3AddressUtil {
    private final GeoServerProperties geoServerProperties;
    private final S3Properties s3Properties;

    public String getS3Address(String path) {
        return geoServerProperties.getEndpoint() + "://" + s3Properties.getBucket() + "/" + path;
    }
}
