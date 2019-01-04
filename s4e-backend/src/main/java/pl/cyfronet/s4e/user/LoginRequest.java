package pl.cyfronet.s4e.user;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
