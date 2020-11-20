package pl.cyfronet.s4e.license.types;

import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.security.AppUserDetails;

public interface WritableLicense extends ProductGranularLicense {
    /**
     * Decide whether to allow writing product grants by user.
     * <p>
     * No further information fetching should occur in implementations.
     *
     * @param product
     * @param userDetails
     * @return
     */
    boolean canWrite(Product product, AppUserDetails userDetails);
}
