package pl.cyfronet.s4e.controller.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class JsonStringValidatorTest {
    @ParameterizedTest
    @MethodSource
    public void shouldValidateJson(String value, boolean correct) {
        JsonStringValidator validator = new JsonStringValidator();

        assertThat(validator.isValid(value, null), is(correct));
    }

    private static Stream<Arguments> shouldValidateJson() {
        return Stream.of(
                Arguments.of(null, true),
                Arguments.of("", false),
                Arguments.of(" ", false),
                Arguments.of("12", false),
                Arguments.of("\"sth\"", false),
                Arguments.of("{\"test\":[1,-2.5,\"else\"]}", true),
                Arguments.of("{\"test\":[1,-2.5,\"else]}", false),
                Arguments.of("{\"test\":[1,-2.5,else]}", false)
        );
    }

}
