package pl.cyfronet.s4e;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SpringBootTest
@ActiveProfiles("test")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BasicTest {
}
