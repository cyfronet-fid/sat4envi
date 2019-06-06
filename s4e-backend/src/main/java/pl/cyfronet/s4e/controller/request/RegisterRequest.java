package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
public class RegisterRequest {
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    @Size(min=8)
    private String password;
}
