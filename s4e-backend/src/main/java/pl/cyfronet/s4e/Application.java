package pl.cyfronet.s4e;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@OpenAPIDefinition(
        info = @Info(
                title = "Sat4Envi backend API",
                description = "The API documentation of the backend.",
                version = "v1"
        )
)
@SpringBootApplication
@ConfigurationPropertiesScan("pl.cyfronet.s4e")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
