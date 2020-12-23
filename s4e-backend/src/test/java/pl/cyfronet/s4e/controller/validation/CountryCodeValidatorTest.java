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

class CountryCodeValidatorTest {
    @ParameterizedTest
    @MethodSource
    public void shouldValidateCode(String code, boolean correct) {
        CountryCodeValidator validator = new CountryCodeValidator();

        assertThat(validator.isValid(code, null), is(correct));
    }

    private static Stream<Arguments> shouldValidateCode() {
        return Stream.of(
                // Correct examples.
                Arguments.of("PL",  true),
                Arguments.of("GB",  true),
                // No such country code in standard.
                Arguments.of("AA",  false),
                // Incorrect case.
                Arguments.of("pl",  false),
                Arguments.of("Pl",  false),
                Arguments.of("pL",  false),
                // Incorrect length.
                Arguments.of("POL", false),
                Arguments.of("P",   false),
                Arguments.of("",    false),
                // Allow null.
                Arguments.of(null,  true)
        );
    }
}
