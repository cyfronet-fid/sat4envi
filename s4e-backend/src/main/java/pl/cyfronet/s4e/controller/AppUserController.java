package pl.cyfronet.s4e.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.ex.AppUserCreationException;
import pl.cyfronet.s4e.service.AppUserService;

import javax.validation.Valid;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest registerRequest) throws AppUserCreationException {
        appUserService.save(AppUser.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                // for now it will be only possible to register as the lowest category
                .role(AppRole.CAT1)
                // FIXME: Enabled should be set to false here and only after email has been verified set to true.
                //        See #28.
                .enabled(true)
                .build());

        return ResponseEntity.ok().build();
    }
}
