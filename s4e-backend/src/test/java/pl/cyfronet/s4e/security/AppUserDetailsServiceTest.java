/*
 * Copyright 2021 ACC Cyfronet AGH
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

package pl.cyfronet.s4e.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.bean.Property;
import pl.cyfronet.s4e.data.repository.PropertyRepository;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {
    @Mock private PropertyRepository propertyRepository;

    @InjectMocks
    private AppUserDetailsService auds;

    @BeforeEach
    public void beforeEach() {

    }

    @Test
    public void shouldReturnEmpty() {
        assertThat(auds.getEffectiveAuthorities(Set.of(), Set.of()), is(sga()));
    }

    @Nested
    class WithDefaultEumetsatWhitelist {
        @ParameterizedTest
        @ValueSource(strings = { "ROLE_MEMBER_ZK", "ROLE_MEMBER_PAK" })
        public void shouldAddEumetsatLicense(String authority) {
            assertAddsEumetsatLicense(authority);
        }

        @ParameterizedTest
        @ValueSource(strings = { "ROLE_MEMBER_OTHER" })
        public void shouldOmitEumetsatLicense(String authority) {
            assertDoesntAddEumetsatLicense(authority);
        }
    }

    @Nested
    class WithEumetsatWhitelistWithPak {
        @BeforeEach
        public void beforeEach() {
            when(propertyRepository.findByName(Constants.PROPERTY_EUMETSAT_AUTHORITY_WHITELIST))
                    .thenReturn(Optional.of(Property.builder().value("ROLE_MEMBER_PAK").build()));
        }

        @ParameterizedTest
        @ValueSource(strings = { "ROLE_MEMBER_PAK" })
        public void shouldAddEumetsatLicense(String authority) {
            assertAddsEumetsatLicense(authority);
        }

        @ParameterizedTest
        @ValueSource(strings = { "ROLE_MEMBER_ZK", "ROLE_MEMBER_OTHER" })
        public void shouldOmitEumetsatLicense(String authority) {
            assertDoesntAddEumetsatLicense(authority);
        }
    }

    @Nested
    class WithEumetsatWhitelistWithEmptyString {
        @BeforeEach
        public void beforeEach() {
            when(propertyRepository.findByName(Constants.PROPERTY_EUMETSAT_AUTHORITY_WHITELIST))
                    .thenReturn(Optional.of(Property.builder().value("").build()));
        }

        @ParameterizedTest
        @ValueSource(strings = { "ROLE_MEMBER_PAK", "ROLE_MEMBER_ZK", "ROLE_MEMBER_OTHER" })
        public void shouldOmitEumetsatLicense(String authority) {
            assertDoesntAddEumetsatLicense(authority);
        }
    }

    private void assertAddsEumetsatLicense(String authority) {
        assertThat(auds.getEffectiveAuthorities(Set.of(authority), Set.of()), is(sga(authority, "LICENSE_EUMETSAT")));
    }

    private void assertDoesntAddEumetsatLicense(String authority) {
        assertThat(auds.getEffectiveAuthorities(Set.of(authority), Set.of()), is(sga(authority)));
    }

    private Set<SimpleGrantedAuthority> sga(String... authorities) {
        return Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}
