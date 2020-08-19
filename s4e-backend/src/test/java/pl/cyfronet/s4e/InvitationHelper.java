package pl.cyfronet.s4e;

import com.github.slugify.Slugify;
import lombok.val;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.controller.request.InvitationRequest;
import pl.cyfronet.s4e.controller.request.InvitationResendInvitation;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InvitationHelper {
    private static final AtomicInteger COUNT = new AtomicInteger();

    private static final String institutionName = "Institution %d";

    private static final String email = "test-%d@mail.pl";
    private static final String profileName = "Test user %d";
    private static final String password = "password";

    private static final Slugify slugify = new Slugify();

    public static Institution.InstitutionBuilder institutionBuilder() {
        val name = nextUnique(InvitationHelper.institutionName);
        return Institution.builder()
                .name(name)
                .slug(slugify.slugify(name));
    }

    public static Group.GroupBuilder defaultGroupBuilder(Institution institution) {
        return Group
                .builder()
                .name("__default__")
                .slug("default")
                .institution(institution);
    }

    public static AppUser.AppUserBuilder userBuilder() {
        val name = String.format(profileName, COUNT.getAndIncrement());
        val profileEmail = String.format(email, COUNT.getAndIncrement());
        return AppUser.builder()
                .id((long) COUNT.getAndIncrement())
                .email(profileEmail)
                .name(name)
                .surname(name)
                .password(password)
                .enabled(true);
    }

    public static Invitation.InvitationBuilder invitationBuilder(Institution institution) {
        val invitationEmail = String.format(email, COUNT.getAndIncrement());
        return Invitation.builder()
                .email(invitationEmail)
                .institution(institution)
                .status(InvitationStatus.WAITING)
                .token(UUID.randomUUID().toString());
    }

    public static InvitationRequest.InvitationRequestBuilder invitationRequestBuilder() {
        val invitationEmail = String.format(email, COUNT.getAndIncrement());
        return InvitationRequest
                .builder()
                .email(invitationEmail);

    }

    public static InvitationResendInvitation.InvitationResendInvitationBuilder invitationResendInvitationBuilder(String oldEmail) {
        val newEmail = String.format(email, COUNT.getAndIncrement());
        return InvitationResendInvitation
                .builder()
                .oldEmail(oldEmail)
                .newEmail(newEmail);
    }

    private static String nextUnique(String format) {
        return String.format(format, COUNT.getAndIncrement());
    }
}
