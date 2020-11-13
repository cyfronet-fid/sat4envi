package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
public class PasswordResetRequest {
    @NotEmpty
    @Size(min=8)
    private String password;

    @NotEmpty
    private String token;
}
