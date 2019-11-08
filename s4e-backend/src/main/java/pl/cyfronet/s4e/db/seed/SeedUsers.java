package pl.cyfronet.s4e.db.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.EmailVerificationRepository;

import java.util.Arrays;
import java.util.List;

@Profile({"development", "run-seed-users"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedUsers implements ApplicationRunner {
    private final AppUserRepository appUserRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    private final PasswordEncoder passwordEncoder;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        emailVerificationRepository.deleteAll();
        appUserRepository.deleteAll();

        seedUsers();

        log.info("Seeding users complete");
    }

    private void seedUsers() {
        log.info("Seeding AppUsers");
        List<AppUser> appUsers = Arrays.asList(new AppUser[]{
                AppUser.builder()
                        .email("cat1user@mail.pl")
                        .name("Name1")
                        .surname("Surname1")
                        .password(passwordEncoder.encode("cat1user"))
                        .role(AppRole.CAT1)
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("cat2user@mail.pl")
                        .name("Name2")
                        .surname("Surname2")
                        .password(passwordEncoder.encode("cat2user"))
                        .role(AppRole.CAT1)
                        .role(AppRole.CAT2)
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("cat3user@mail.pl")
                        .name("Name3")
                        .surname("Surname3")
                        .password(passwordEncoder.encode("cat3user"))
                        .role(AppRole.CAT1)
                        .role(AppRole.CAT2)
                        .role(AppRole.CAT3)
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("cat4user@mail.pl")
                        .name("Name3")
                        .surname("Surname4")
                        .password(passwordEncoder.encode("cat4user"))
                        .role(AppRole.CAT1)
                        .role(AppRole.CAT2)
                        .role(AppRole.CAT3)
                        .role(AppRole.CAT4)
                        .enabled(true)
                        .build(),
        });
        appUserRepository.saveAll(appUsers);
    }
}
