package pl.cyfronet.s4e.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import static pl.cyfronet.s4e.security.SecurityConstants.LICENSE_READ_AUTHORITY_PREFIX;

@RequiredArgsConstructor
@Slf4j
public class LicenseHelper {
    private final ProductRepository productRepository;
    private final String geoServerWorkspace;

    private interface Projection {
        Long getId();
        String getLayerName();
    }

    public String readLicenseAuthorityToLayerName(GrantedAuthority grantedAuthority) throws NumberFormatException, NotFoundException {
        String authorityText = grantedAuthority.getAuthority();

        if (!authorityText.startsWith(LICENSE_READ_AUTHORITY_PREFIX)) {
            return null;
        }

        String productIdText = authorityText.substring(LICENSE_READ_AUTHORITY_PREFIX.length());
        long productId = Long.parseLong(productIdText);
        Projection product = productRepository.findById(productId, Projection.class)
                .orElseThrow(() -> new NotFoundException("Product with id '" + productId + "' not found"));
        return geoServerWorkspace + ":" + product.getLayerName();
    }
}
