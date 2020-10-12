package pl.cyfronet.s4e.license;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.license.types.EumetsatLicense;
import pl.cyfronet.s4e.license.types.OpenLicense;
import pl.cyfronet.s4e.license.types.PrivateLicense;
import pl.cyfronet.s4e.license.types.ProductGranularLicense;
import pl.cyfronet.s4e.properties.GeoServerProperties;
import pl.cyfronet.s4e.util.LicenseHelper;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;

@Configuration
public class LicenseConfig {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private GeoServerProperties geoServerProperties;

    @Autowired
    private Clock clock;

    @Bean
    public LicenseHelper licenseHelper() {
        return new LicenseHelper(productRepository, geoServerProperties.getWorkspace());
    }

    @Bean
    public Map<Product.AccessType, ProductGranularLicense> productLicenses() {
        return Map.of(
                Product.AccessType.OPEN, new OpenLicense(),
                Product.AccessType.PRIVATE, new PrivateLicense(),
                Product.AccessType.EUMETSAT, new EumetsatLicense(clock, Duration.ofHours(3))
        );
    }

    @Bean
    public LicensePermissionEvaluator licensePermissionEvaluator() {
        return new LicensePermissionEvaluator(productLicenses(), productRepository, sceneRepository);
    }
}
