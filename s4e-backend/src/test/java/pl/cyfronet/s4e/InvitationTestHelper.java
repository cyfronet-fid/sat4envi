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

package pl.cyfronet.s4e;

import com.github.slugify.Slugify;
import lombok.val;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.bean.Invitation;
import pl.cyfronet.s4e.bean.InvitationStatus;
import pl.cyfronet.s4e.controller.request.InvitationRequest;
import pl.cyfronet.s4e.controller.request.InvitationResendInvitation;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InvitationTestHelper {
    private static final AtomicInteger COUNT = new AtomicInteger();

    private static final String institutionName = "Institution %d";

    private static final String email = "test-%d@mail.pl";
    private static final String profileName = "Test user %d";
    private static final String password = "password";

    private static final Slugify slugify = Slugify.builder().build();

    public static Institution.InstitutionBuilder institutionBuilder() {
        val name = nextUnique(InvitationTestHelper.institutionName);
        return Institution.builder()
                .name(name)
                .slug(slugify.slugify(name));
    }

    public static AppUser.AppUserBuilder userBuilder() {
        val name = String.format(Locale.ENGLISH, profileName, COUNT.getAndIncrement());
        val profileEmail = String.format(Locale.ENGLISH, email, COUNT.getAndIncrement());
        return AppUser.builder()
                .email(profileEmail)
                .name(name)
                .surname(name)
                .password(password)
                .enabled(true);
    }

    public static Invitation.InvitationBuilder invitationBuilder(Institution institution) {
        val invitationEmail = String.format(Locale.ENGLISH, email, COUNT.getAndIncrement());
        return Invitation.builder()
                .email(invitationEmail)
                .institution(institution)
                .status(InvitationStatus.WAITING)
                .token(UUID.randomUUID().toString());
    }

    public static InvitationRequest.InvitationRequestBuilder invitationRequestBuilder() {
        val invitationEmail = String.format(Locale.ENGLISH, email, COUNT.getAndIncrement());
        return InvitationRequest
                .builder()
                .email(invitationEmail);

    }

    public static InvitationResendInvitation.InvitationResendInvitationBuilder invitationResendInvitationBuilder(String oldEmail) {
        val newEmail = String.format(Locale.ENGLISH, email, COUNT.getAndIncrement());
        return InvitationResendInvitation
                .builder()
                .oldEmail(oldEmail)
                .newEmail(newEmail);
    }

    private static String nextUnique(String format) {
        return String.format(Locale.ENGLISH, format, COUNT.getAndIncrement());
    }
}
