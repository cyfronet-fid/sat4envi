package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class PasswordResetRequest {
    @NotEmpty
    private String password;
    @NotEmpty
    private String token;
}
