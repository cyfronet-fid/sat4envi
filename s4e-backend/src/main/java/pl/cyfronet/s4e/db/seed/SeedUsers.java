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
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.EmailVerificationRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.InstitutionCreationException;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.GroupService;
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
    private final GroupService groupService;
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
                        .email("zkPLManager@mail.pl")
                        .name("Jakub")
                        .surname("Kowalski")
                        .password(passwordEncoder.encode("zkPLManager"))
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
                        .email("zkMazManager@mail.pl")
                        .name("Mirosław")
                        .surname("Koło")
                        .password(passwordEncoder.encode("zkMazManager"))
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
                        .email("zkWawManager@mail.pl")
                        .name("Andrzej")
                        .surname("Dach")
                        .password(passwordEncoder.encode("zkWawManager"))
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
                        .email("zkMalManager@mail.pl")
                        .name("Rafał")
                        .surname("Kowal")
                        .password(passwordEncoder.encode("zkMalManager"))
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
                        .email("zkKrManager@mail.pl")
                        .name("Piotr")
                        .surname("Okno")
                        .password(passwordEncoder.encode("zkKrManager"))
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
                    .build());
            groupService.addMember(institution.getSlug(), "default", "zkMember@mail.pl");
            groupService.addMember(institution.getSlug(), "default", "zkAdmin@mail.pl");
            userRoleService.addRole(AppRole.INST_ADMIN, "zkAdmin@mail.pl", institution.getSlug(), "default");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkAdmin@mail.pl", institution.getSlug(), "default");
            userRoleService.addRole(AppRole.GROUP_MANAGER, "zkAdmin@mail.pl", institution.getSlug(), "default");

            groupService.addMember(institution.getSlug(), "default", "zkPLMember@mail.pl");
            groupService.addMember(institution.getSlug(), "default", "zkPLManager@mail.pl");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkPLManager@mail.pl", institution.getSlug(), "default");
            groupService.addMember(institution.getSlug(), "default", "zkPLAdmin@mail.pl");
            userRoleService.addRole(AppRole.INST_ADMIN, "zkPLAdmin@mail.pl", institution.getSlug(), "default");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkPLAdmin@mail.pl", institution.getSlug(), "default");

            name = "Zarządzanie kryzysowe - Mazowieckie";
            Institution childInstitution = institutionService.save(Institution.builder()
                    .name(name)
                    .slug(slugService.slugify(name))
                    .parent(institution)
                    .build());
            groupService.addMember(childInstitution.getSlug(), "default", "zkMazMember@mail.pl");
            groupService.addMember(childInstitution.getSlug(), "default", "zkMazManager@mail.pl");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkMazManager@mail.pl", childInstitution.getSlug(), "default");
            groupService.addMember(childInstitution.getSlug(), "default", "zkMazAdmin@mail.pl");
            userRoleService.addRole(AppRole.INST_ADMIN, "zkMazAdmin@mail.pl", childInstitution.getSlug(), "default");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkMazAdmin@mail.pl", childInstitution.getSlug(), "default");

            name = "Zarządzanie kryzysowe - Warszawa";
            Institution child2RowInstitution = institutionService.save(Institution.builder()
                    .name(name)
                    .slug(slugService.slugify(name))
                    .parent(childInstitution)
                    .build());
            groupService.addMember(child2RowInstitution.getSlug(), "default", "zkWawMember@mail.pl");
            groupService.addMember(child2RowInstitution.getSlug(), "default", "zkWawManager@mail.pl");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkWawManager@mail.pl", child2RowInstitution.getSlug(), "default");
            groupService.addMember(child2RowInstitution.getSlug(), "default", "zkWawAdmin@mail.pl");
            userRoleService.addRole(AppRole.INST_ADMIN, "zkWawAdmin@mail.pl", child2RowInstitution.getSlug(), "default");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkWawAdmin@mail.pl", child2RowInstitution.getSlug(), "default");

            name = "Zarządzanie kryzysowe - Małopolska";
            childInstitution = institutionService.save(Institution.builder()
                    .name(name)
                    .slug(slugService.slugify(name))
                    .parent(institution)
                    .build());
            groupService.addMember(childInstitution.getSlug(), "default", "zkMalMember@mail.pl");
            groupService.addMember(childInstitution.getSlug(), "default", "zkMalManager@mail.pl");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkMalManager@mail.pl", childInstitution.getSlug(), "default");
            groupService.addMember(childInstitution.getSlug(), "default", "zkMalAdmin@mail.pl");
            userRoleService.addRole(AppRole.INST_ADMIN, "zkMalAdmin@mail.pl", childInstitution.getSlug(), "default");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkMalAdmin@mail.pl", childInstitution.getSlug(), "default");

            name = "Zarządzanie kryzysowe - Kraków";
            child2RowInstitution = institutionService.save(Institution.builder()
                    .name(name)
                    .slug(slugService.slugify(name))
                    .parent(childInstitution)
                    .build());
            groupService.addMember(child2RowInstitution.getSlug(), "default", "zkKrMember@mail.pl");
            groupService.addMember(child2RowInstitution.getSlug(), "default", "zkKrManager@mail.pl");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkKrManager@mail.pl", child2RowInstitution.getSlug(), "default");
            groupService.addMember(child2RowInstitution.getSlug(), "default", "zkKrAdmin@mail.pl");
            userRoleService.addRole(AppRole.INST_ADMIN, "zkKrAdmin@mail.pl", child2RowInstitution.getSlug(), "default");
            userRoleService.addRole(AppRole.INST_MANAGER, "zkKrAdmin@mail.pl", child2RowInstitution.getSlug(), "default");

        } catch (InstitutionCreationException e) {
            log.warn(e.getMessage(), e);
        } catch (NotFoundException e) {
            log.warn(e.getMessage(), e);
        }
    }
}
