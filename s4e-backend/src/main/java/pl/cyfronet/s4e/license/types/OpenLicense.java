package pl.cyfronet.s4e.license.types;

import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.security.AppUserDetails;

public class OpenLicense implements ProductGranularLicense {
    @Override
    public boolean canRead(Product product, AppUserDetails userDetails) {
        return true;
    }
}
