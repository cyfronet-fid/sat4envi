package pl.cyfronet.s4e.controller.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
