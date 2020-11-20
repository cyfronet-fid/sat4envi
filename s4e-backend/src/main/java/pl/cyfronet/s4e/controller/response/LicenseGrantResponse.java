package pl.cyfronet.s4e.controller.response;

import org.springframework.beans.factory.annotation.Value;

public interface LicenseGrantResponse {
    @Value("#{target.institution.slug}")
    String getInstitutionSlug();

    @Value("#{target.product.id}")
    Long getProductId();

    Boolean getOwner();
}
