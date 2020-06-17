package pl.cyfronet.s4e.controller.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImageDimensionsValidatorTest {
    @ParameterizedTest
    @MethodSource
    public void shouldValidateSize(int maxWidth, int maxHeight, boolean correct) {
        ImageDimensionsValidator validator = new ImageDimensionsValidator();
        ImageDimensions annotation = mock(ImageDimensions.class);
        when(annotation.maxWidth()).thenReturn(maxWidth);
        when(annotation.maxHeight()).thenReturn(maxHeight);
        validator.initialize(annotation);

        assertThat(validator.isValid(ValidationResources.IMAGE_JPEG_96x96, null), is(correct));
    }

    private static Stream<Arguments> shouldValidateSize() {
        return Stream.of(
                Arguments.of(100, 100, true),
                Arguments.of(96,  96,  true),
                Arguments.of(96,  95,  false),
                Arguments.of(95,  96,  false),
                Arguments.of(95,  95,  false),
                Arguments.of(10,  10,  false)
        );
    }

    @Test
    public void shouldAllowNullValue() {
        ImageDimensionsValidator validator = new ImageDimensionsValidator();
        ImageDimensions annotation = mock(ImageDimensions.class);
        validator.initialize(annotation);

        assertThat(validator.isValid(null, null), is(true));
    }

}
