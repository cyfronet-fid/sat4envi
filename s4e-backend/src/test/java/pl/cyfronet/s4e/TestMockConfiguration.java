package pl.cyfronet.s4e;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.cyfronet.s4e.service.FileStorage;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test & !integration")
public class TestMockConfiguration {
    @Bean
    public FileStorage fileStorage() {
        return mock(FileStorage.class);
    }
}
