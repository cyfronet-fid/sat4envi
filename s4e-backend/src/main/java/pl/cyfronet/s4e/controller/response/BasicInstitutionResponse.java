package pl.cyfronet.s4e.controller.response;

import org.springframework.beans.factory.annotation.Value;

public interface BasicInstitutionResponse {
    String getName();

    String getSlug();

    @Value("#{@institutionService.getParentSlugBy(target.slug)}")
    String getParentSlug();
}
