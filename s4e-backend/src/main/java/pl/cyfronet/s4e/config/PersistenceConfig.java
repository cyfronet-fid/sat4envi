package pl.cyfronet.s4e.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class PersistenceConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(AppUserDetailsSupplier.get())
                .map(AppUserDetails::getUsername);
    }
}
