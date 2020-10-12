package pl.cyfronet.s4e.license.types;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.security.AppUserDetails;

import static pl.cyfronet.s4e.security.AppUserDetailsUtil.isAdmin;
import static pl.cyfronet.s4e.security.SecurityConstants.LICENSE_READ_AUTHORITY_PREFIX;

public class PrivateLicense implements ProductGranularLicense {
    @Override
    public boolean canRead(Product product, AppUserDetails userDetails) {
        if (isAdmin(userDetails)) {
            return true;
        }

        return hasLicense(userDetails, product);
    }

    private boolean hasLicense(AppUserDetails userDetails, Product product) {
        return userDetails != null && userDetails.getAuthorities()
                .contains(new SimpleGrantedAuthority(LICENSE_READ_AUTHORITY_PREFIX + product.getId().toString()));
    }
}
