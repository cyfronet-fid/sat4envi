package pl.cyfronet.s4e.admin.license;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
class AdminCreateLicenseGrantRequest {
    @NotEmpty
    private String institutionSlug;

    @NotNull
    private Long productId;

    private boolean owner;
}
