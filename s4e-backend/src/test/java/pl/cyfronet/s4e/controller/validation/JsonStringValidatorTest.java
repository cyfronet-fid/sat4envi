/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
