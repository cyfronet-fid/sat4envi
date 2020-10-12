package pl.cyfronet.s4e.license.types;

import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.security.AppUserDetails;

public interface ProductGranularLicense {
    /**
     * Method to decide whether to allow reading the product by a given user.
     * <p>
     * Decision should be made solely based on the passed arguments.
     *
     * @param product
     * @param userDetails
     * @return
     */
    boolean canRead(Product product, AppUserDetails userDetails);
}
