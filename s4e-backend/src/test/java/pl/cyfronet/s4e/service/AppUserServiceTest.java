package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.ex.AppUserDuplicateException;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BasicTest
@Slf4j
class AppUserServiceTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserService appUserService;

    @BeforeEach
    public void beforeEach() {
        appUserRepository.deleteAll();
    }

    @Test
    public void shouldProhibitDuplicateEmail() {
        String email = "test@some.pl";
        appUserRepository.save(AppUser.builder()
                .email(email)
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .build());

        assertThat(appUserRepository.findByEmail(email), isPresent());

        assertThrows(
                AppUserDuplicateException.class,
                () -> appUserService.register(RegisterRequest.builder()
                        .name("Name")
                        .surname("Surname")
                        .email(email)
                        .password("someHash")
                        .build())
        );
    }

}
