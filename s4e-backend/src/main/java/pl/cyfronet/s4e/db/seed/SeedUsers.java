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
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.controller.request.CreateChildInstitutionRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.EmailVerificationRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.InstitutionService;
import pl.cyfronet.s4e.service.SlugService;
import pl.cyfronet.s4e.service.UserRoleService;

import java.util.Arrays;
import java.util.List;

@Profile({"development", "run-seed-users"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedUsers implements ApplicationRunner {
    private final AppUserRepository appUserRepository;
    private final InstitutionRepository institutionRepository;
    private final InstitutionService institutionService;
    private final UserRoleService userRoleService;
    private final SlugService slugService;
    private final EmailVerificationRepository emailVerificationRepository;

    private final PasswordEncoder passwordEncoder;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        emailVerificationRepository.deleteAll();
        institutionRepository.deleteAll();
        appUserRepository.deleteAll();

        seedUsers();
        seedInstitutionsAndRoles();

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
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("cat2user@mail.pl")
                        .name("Name2")
                        .surname("Surname2")
                        .password(passwordEncoder.encode("cat2user"))
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("cat3user@mail.pl")
                        .name("Name3")
                        .surname("Surname3")
                        .password(passwordEncoder.encode("cat3user"))
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("cat4user@mail.pl")
                        .name("Name4")
                        .surname("Surname4")
                        .password(passwordEncoder.encode("cat4user"))
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .email("zkMember@mail.pl")
                        .name("Name5")
                        .surname("Surname5")
                        .password(passwordEncoder.encode("zkMember"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                AppUser.builder()
                        .email("zkAdmin@mail.pl")
                        .name("Name6")
                        .surname("Surname6")
                        .password(passwordEncoder.encode("zkAdmin20"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                AppUser.builder()
                        .email("admin@mail.pl")
                        .name("Name7")
                        .surname("Surname7")
                        .password(passwordEncoder.encode("adminPass20"))
                        .enabled(true)
                        .admin(true)
                        .build(),
                // ZK - PL
                AppUser.builder()
                        .email("zkPLAdmin@mail.pl")
                        .name("Jan")
                        .surname("Nowak")
                        .password(passwordEncoder.encode("zkPLAdmin"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                AppUser.builder()
                        .email("zkPLMember@mail.pl")
                        .name("Michał")
                        .surname("Buda")
                        .password(passwordEncoder.encode("zkPLMember"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                // ZK - Maz
                AppUser.builder()
                        .email("zkMazAdmin@mail.pl")
                        .name("Maciek")
                        .surname("Poważny")
                        .password(passwordEncoder.encode("zkMazAdmin"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                AppUser.builder()
                        .email("zkMazMember@mail.pl")
                        .name("Bartosz")
                        .surname("Kulka")
                        .password(passwordEncoder.encode("zkMazMember"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                // ZK - Waw
                AppUser.builder()
                        .email("zkWawAdmin@mail.pl")
                        .name("Wojciech")
                        .surname("Zaradny")
                        .password(passwordEncoder.encode("zkWawAdmin"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                AppUser.builder()
                        .email("zkWawMember@mail.pl")
                        .name("Krzysztof")
                        .surname("Duka")
                        .password(passwordEncoder.encode("zkWawMember"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                // ZK - Mał
                AppUser.builder()
                        .email("zkMalAdmin@mail.pl")
                        .name("Benedykt")
                        .surname("Biały")
                        .password(passwordEncoder.encode("zkMalAdmin"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                AppUser.builder()
                        .email("zkMalMember@mail.pl")
                        .name("Jarosław")
                        .surname("Bulba")
                        .password(passwordEncoder.encode("zkMalMember"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                // ZK - KR
                AppUser.builder()
                        .email("zkKrAdmin@mail.pl")
                        .name("Tomasz")
                        .surname("Kłopotek")
                        .password(passwordEncoder.encode("zkKrAdmin"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
                AppUser.builder()
                        .email("zkKrMember@mail.pl")
                        .name("Paweł")
                        .surname("Zamek")
                        .password(passwordEncoder.encode("zkKrMember"))
                        .enabled(true)
                        .memberZK(true)
                        .build(),
        });
        appUserRepository.saveAll(appUsers);
    }

    private void seedInstitutionsAndRoles() {
        log.info("Seeding Institutions & Roles");
        try {
            String name = "Zarządzenie kryzysowe - PL";
            Institution institution = institutionService.save(Institution.builder()
                    .name(name)
                    .slug(slugService.slugify(name))
                    .city("Warszawa")
                    .build());
            institutionService.addMember(institution.getSlug(), "zkMember@mail.pl");
            institutionService.addMember(institution.getSlug(), "zkAdmin@mail.pl");
            userRoleService.addRole(AppRole.INST_ADMIN, "zkAdmin@mail.pl", institution.getSlug());

            institutionService.addMember(institution.getSlug(), "zkPLMember@mail.pl");
            institutionService.addMember(institution.getSlug(), "zkPLAdmin@mail.pl");
            userRoleService.addRole(AppRole.INST_ADMIN, "zkPLAdmin@mail.pl", institution.getSlug());
            institutionService.addMember(institution.getSlug(), "admin@mail.pl");
            userRoleService.addRole(AppRole.INST_ADMIN, "admin@mail.pl", institution.getSlug());

            name = "Zarządzanie kryzysowe - Mazowieckie";
            CreateChildInstitutionRequest zkMazRequest = CreateChildInstitutionRequest.builder()
                    .institutionAdminEmail("zkMazAdmin@mail.pl")
                    .name(name)
                    .city("Warszawa")
                    .build();
            Institution childInstitution = institutionService.createChildInstitution(zkMazRequest, institution.getSlug());
            institutionService.addMember(childInstitution.getSlug(), "zkMazMember@mail.pl");

            name = "Zarządzanie kryzysowe - Warszawa";
            CreateChildInstitutionRequest zkWawRequest = CreateChildInstitutionRequest.builder()
                    .institutionAdminEmail("zkWawAdmin@mail.pl")
                    .name(name)
                    .city("Warszawa")
                    .build();
            Institution child2RowInstitution = institutionService.createChildInstitution(zkWawRequest, childInstitution.getSlug());
            institutionService.addMember(child2RowInstitution.getSlug(), "zkWawMember@mail.pl");

            name = "Zarządzanie kryzysowe - Małopolska";
            CreateChildInstitutionRequest zkMalRequest = CreateChildInstitutionRequest.builder()
                    .institutionAdminEmail("zkMalAdmin@mail.pl")
                    .name(name)
                    .city("Kraków")
                    .build();
            childInstitution = institutionService.createChildInstitution(zkMalRequest, institution.getSlug());
            institutionService.addMember(childInstitution.getSlug(), "zkMalMember@mail.pl");

            name = "Zarządzanie kryzysowe - Kraków";
            CreateChildInstitutionRequest zkKrRequest = CreateChildInstitutionRequest.builder()
                    .institutionAdminEmail("zkKrAdmin@mail.pl")
                    .name(name)
                    .city("Kraków")
                    .build();
            child2RowInstitution = institutionService.createChildInstitution(zkKrRequest, childInstitution.getSlug());
            institutionService.addMember(child2RowInstitution.getSlug(), "zkKrAdmin@mail.pl");


        } catch (InstitutionCreationException e) {
            log.warn(e.getMessage(), e);
        } catch (NotFoundException e) {
            log.warn(e.getMessage(), e);
        }
    }
}
