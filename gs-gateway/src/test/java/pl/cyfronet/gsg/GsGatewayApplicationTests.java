package pl.cyfronet.gsg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.profiles.active=development"
})
class GsGatewayApplicationTests {
    @Test
    public void contextLoads() { }
}
