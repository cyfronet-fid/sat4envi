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

package pl.cyfronet.s4e.db;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.security.AppUserDetails;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@BasicTest
public class AuditingTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();
    }

    @AfterEach
    public void afterEach() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldRecordSave() {
        Long creatorId = 1000L;
        authenticateAs(creatorId);

        LocalDateTime beforeSave = LocalDateTime.now();

        AppUser appUser = saveAppUser("test@some.pl");

        LocalDateTime afterSave = LocalDateTime.now();

        assertThat(appUser.getCreatedAt(), isInRange(beforeSave, afterSave));
        assertThat(appUser.getCreatedBy(), is(equalTo(creatorId)));
        assertThat(appUser.getLastModifiedAt(), isInRange(beforeSave, afterSave));
        assertThat(appUser.getLastModifiedBy(), is(equalTo(creatorId)));
    }

    @Test
    public void shouldRecordSaveIfUnauthenticated() {
        LocalDateTime beforeSave = LocalDateTime.now();

        AppUser appUser = saveAppUser("test@some.pl");

        LocalDateTime afterSave = LocalDateTime.now();

        assertThat(appUser.getCreatedAt(), isInRange(beforeSave, afterSave));
        assertThat(appUser.getCreatedBy(), is(nullValue()));
        assertThat(appUser.getLastModifiedAt(), isInRange(beforeSave, afterSave));
        assertThat(appUser.getLastModifiedBy(), is(nullValue()));
    }

    @Test
    public void shouldRecordUpdate() {
        String userEmail = "test@some.pl";
        Long creatorId = 1000L;
        Long modifierId = 100L;
        authenticateAs(creatorId);
        saveAppUser(userEmail);
        authenticateAs(modifierId);

        LocalDateTime beforeModification = LocalDateTime.now();

        AppUser appUser = updateAppUser(userEmail);

        LocalDateTime afterModification = LocalDateTime.now();

        assertThat(appUser.getCreatedAt(), is(lessThan(beforeModification)));
        assertThat(appUser.getCreatedBy(), is(equalTo(creatorId)));
        assertThat(appUser.getLastModifiedAt(), isInRange(beforeModification, afterModification));
        assertThat(appUser.getLastModifiedBy(), is(equalTo(modifierId)));
    }

    private AppUser saveAppUser(String email) {
        return appUserRepository.save(createAppUser(email));
    }

    private AppUser updateAppUser(String email) {
        AppUser appUser = appUserRepository.findByEmail(email).get();
        appUser.setName(appUser.getName() + " changed");
        return appUserRepository.save(appUser);
    }

    private Matcher<LocalDateTime> isInRange(LocalDateTime start, LocalDateTime end) {
        return is(allOf(greaterThan(start), lessThan(end)));
    }

    private AppUser createAppUser(String email) {
        return AppUser.builder()
                .email(email)
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .build();
    }

    private void authenticateAs(Long id) {
        AppUserDetails appUserDetails = mock(AppUserDetails.class);
        when(appUserDetails.getId()).thenReturn(id);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getDetails()).thenReturn(appUserDetails);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }
}
