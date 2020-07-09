package pl.cyfronet.s4e.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String email;
    private String token;
}
