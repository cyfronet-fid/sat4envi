package pl.cyfronet.s4e.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.validation.CountryCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class RegisterRequest {
    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String name;

    @NotEmpty
    private String surname;

    @NotEmpty
    @Size(min=8)
    private String password;

    @NotNull
    @Schema(description = "User's scientific domain")
    private AppUser.ScientificDomain domain;

    @NotNull
    @Schema(description = "User's usage scenario")
    private AppUser.Usage usage;

    @NotNull
    @CountryCode
    @Schema(description = "User's ISO 3166 alpha-2 country code", example = "PL")
    private String country;
}
