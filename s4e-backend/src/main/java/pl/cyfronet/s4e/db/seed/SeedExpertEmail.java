package pl.cyfronet.s4e.db.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.Property;
import pl.cyfronet.s4e.data.repository.PropertyRepository;

@Profile({"development", "run-seed-expert-email"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedExpertEmail implements ApplicationRunner {
    private static final String DEVELOPMENT_EXPERT_HELP_EMAIL = "expert-help@mail.pl";

    private final PropertyRepository propertyRepository;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        if (propertyRepository.findByName(Constants.PROPERTY_EXPERT_HELP_EMAIL).isPresent()) {
            log.info("Expert email already set, skipping");
            return;
        }

        propertyRepository.save(Property.builder()
                .name(Constants.PROPERTY_EXPERT_HELP_EMAIL)
                .value(DEVELOPMENT_EXPERT_HELP_EMAIL)
                .build());

        log.info("Expert email set");
    }
}
