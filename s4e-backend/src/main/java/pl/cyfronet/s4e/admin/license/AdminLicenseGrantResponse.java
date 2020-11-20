package pl.cyfronet.s4e.admin.license;

import org.springframework.beans.factory.annotation.Value;

interface AdminLicenseGrantResponse {
    Long getId();

    @Value("#{target.institution.slug}")
    String getInstitutionSlug();

    @Value("#{target.product.id}")
    Long getProductId();

    Boolean getOwner();
}
