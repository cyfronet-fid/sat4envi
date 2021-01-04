/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.license;

import lombok.RequiredArgsConstructor;
import lombok.val;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.Scene;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.SceneRepository;
import pl.cyfronet.s4e.license.types.ProductGranularLicense;
import pl.cyfronet.s4e.license.types.TimestampGranularLicense;
import pl.cyfronet.s4e.license.types.WritableLicense;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.util.Map;

@RequiredArgsConstructor
public class LicensePermissionEvaluator {
    private final Map<Product.AccessType, ProductGranularLicense> productLicenses;
    private final ProductRepository productRepository;
    private final SceneRepository sceneRepository;

    public boolean allowProductWrite(Long productId, Object principal) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return false;
        }
        AppUserDetails userDetails = getNullableUserDetails(principal);
        return decideWrite(userDetails, product);
    }

    public boolean allowProductRead(Long productId, Object principal) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return true;
        }
        AppUserDetails userDetails = getNullableUserDetails(principal);
        return decideRead(userDetails, product);
    }

    public boolean allowSceneRead(Long sceneId, Object principal) {
        Scene scene = sceneRepository.findByIdFetchProduct(sceneId).orElse(null);
        if (scene == null) {
            return true;
        }
        AppUserDetails userDetails = getNullableUserDetails(principal);
        return decideRead(userDetails, scene);
    }

    /**
     * Decide whether to allow write access to {@code product} given authorities in {@code userDetails}.
     *
     * @param userDetails the user details
     * @param product product to authorize
     * @return If given the authorities from {@code userDetails} to allow write access to {@code product}.
     */
    private boolean decideWrite(AppUserDetails userDetails, Product product) {
        ProductGranularLicense license = productLicenses.get(product.getAccessType());
        if (license instanceof WritableLicense) {
            return ((WritableLicense) license).canWrite(product, userDetails);
        } else {
            return false;
        }
    }

    /**
     * Decide whether to allow access to {@code product} given authorities in {@code userDetails}.
     *
     * @param userDetails the user details
     * @param product product to authorize
     * @return If given the authorities from {@code userDetails} to allow read access to {@code product}.
     */
    private boolean decideRead(AppUserDetails userDetails, Product product) {
        if (product.getAuthorizedOnly() && userDetails == null) {
            return false;
        }

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
    private boolean decideRead(AppUserDetails userDetails, Scene scene) {
        Product product = scene.getProduct();

        // Verify access to product.
        if (!decideRead(userDetails, product)) {
            return false;
        }

        ProductGranularLicense license = productLicenses.get(product.getAccessType());

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
