package pl.cyfronet.s4e.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String email;
    private String token;
}
