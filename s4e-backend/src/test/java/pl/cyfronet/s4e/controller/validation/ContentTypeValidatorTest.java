package pl.cyfronet.s4e.controller.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContentTypeValidatorTest {
    @ParameterizedTest
    @MethodSource
    public void shouldValidateContentType(String pattern, boolean correct) {
        ContentTypeValidator validator = new ContentTypeValidator();
        ContentType annotation = mock(ContentType.class);
        when(annotation.pattern()).thenReturn(pattern);
        validator.initialize(annotation);

        assertThat(validator.isValid(ValidationResources.IMAGE_JPEG_96x96, null), is(correct));
    }

    private static Stream<Arguments> shouldValidateContentType() {
        return Stream.of(
                Arguments.of("image/.*", true),
                Arguments.of("other", false)
        );
    }

}
