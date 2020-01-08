package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class CreateChildInstitutionRequest {
    @NotEmpty
    private String name;
    @NotEmpty
    @Email
    private String institutionAdminEmail;
}