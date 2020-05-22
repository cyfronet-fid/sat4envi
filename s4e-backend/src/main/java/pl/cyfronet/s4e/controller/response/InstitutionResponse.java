package pl.cyfronet.s4e.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;

public interface InstitutionResponse extends BasicInstitutionResponse {
    String getAddress();

    String getPostalCode();

    String getCity();

    String getPhone();

    String getSecondaryPhone();

    @Value("#{@institutionService.getEmblemPath(target.slug)}")
    @Schema(description = "a path to the emblem")
    String getEmblem();
}
