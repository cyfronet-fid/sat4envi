package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
public class PasswordChangeRequest {
    @NotEmpty
    private String oldPassword;

    @NotEmpty
    @Size(min=8)
    private String newPassword;
}
