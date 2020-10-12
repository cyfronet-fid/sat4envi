package pl.cyfronet.s4e.license;

import lombok.RequiredArgsConstructor;
import lombok.val;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.license.types.ProductGranularLicense;
import pl.cyfronet.s4e.license.types.TimestampGranularLicense;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.Map;

@RequiredArgsConstructor
public class LicensePermissionEvaluator {
    private final Map<Product.AccessType, ProductGranularLicense> productLicenses;
    private final ProductRepository productRepository;
    private final SceneRepository sceneRepository;

    public boolean allowProductRead(Long productId, Object principal) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return true;
        }
        AppUserDetails userDetails = getNullableUserDetails(principal);
        return decide(userDetails, product);
    }

    public boolean allowSceneRead(Long sceneId, Object principal) {
        Scene scene = sceneRepository.findByIdFetchProduct(sceneId).orElse(null);
        if (scene == null) {
            return true;
        }
        AppUserDetails userDetails = getNullableUserDetails(principal);
        return decide(userDetails, scene);
    }

    /**
     * Decide whether to allow access to {@code product} given authorities in {@code userDetails}.
     *
     * @param userDetails the user details
     * @param product product to authorize
     * @return If given the authorities from {@code userDetails} to allow read access to {@code product}.
     */
    private boolean decide(AppUserDetails userDetails, Product product) {
        ProductGranularLicense license = productLicenses.get(product.getAccessType());
        return license.canRead(product, userDetails);
    }

    /**
     * Decide whether to allow access to {@code scene} scene given authorities in {@code userDetails}.
     * <p>
     * For access to be granted user must be able to read the product associated with the (@code scene}.
     * <p>
     * If product's license is {@link TimestampGranularLicense} then verify the scene access,
     * otherwise pass.
     *
     * @param userDetails the user details
     * @param scene scene to authorize
     * @return If given the authorities from {@code userDetails} to allow read access to {@code scene}
     */
    private boolean decide(AppUserDetails userDetails, Scene scene) {
        Product product = scene.getProduct();
        ProductGranularLicense license = productLicenses.get(product.getAccessType());

        // Verify access to product.
        if (!decide(userDetails, product)) {
            return false;
        }

        // If license is timestamp-granular then verify access,
        if (license instanceof TimestampGranularLicense) {
            val timestampGranularLicense = (TimestampGranularLicense) license;
            return timestampGranularLicense.canRead(scene.getTimestamp(), userDetails);
        // otherwise permit.
        } else {
            return true;
        }
    }

    private AppUserDetails getNullableUserDetails(Object principal) {
        return principal instanceof AppUserDetails ? (AppUserDetails) principal : null;
    }
}
