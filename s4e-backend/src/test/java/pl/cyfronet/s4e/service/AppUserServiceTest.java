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

package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.ex.AppUserDuplicateException;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@BasicTest
@Slf4j
class AppUserServiceTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserService appUserService;

    @BeforeEach
    public void beforeEach() {
        appUserRepository.deleteAll();
    }

    @Test
    public void shouldProhibitDuplicateEmail() {
        String email = "test@some.pl";
        appUserRepository.save(AppUser.builder()
                .email(email)
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .build());

        assertThat(appUserRepository.findByEmail(email), isPresent());

        assertThrows(
                AppUserDuplicateException.class,
                () -> appUserService.register(RegisterRequest.builder()
                        .name("Name")
                        .surname("Surname")
                        .email(email)
                        .password("someHash")
                        .build())
        );
    }

}
