package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkopylec.recaptcha.RecaptchaProperties;
import com.github.mkopylec.recaptcha.validation.ErrorCode;
import com.github.mkopylec.recaptcha.validation.RecaptchaValidator;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.StoredMessage;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.ServerSetupTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.mail.util.MimeMessageParser;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.InvitationHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.event.*;
import pl.cyfronet.s4e.properties.MailProperties;
import pl.cyfronet.s4e.service.SlugService;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static com.icegreen.greenmail.configuration.GreenMailConfiguration.aConfig;
import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.util.Collections.emptyList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;
import static pl.cyfronet.s4e.TestJwtUtil.jwtCookieToken;

@BasicTest
@Slf4j
@AutoConfigureMockMvc
public class AppUserControllerTest {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(aConfig().withDisabledAuthentication());

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private TestListener testListener;

    @SpyBean
    private RecaptchaValidator recaptchaValidator;

    @Autowired
    private RecaptchaProperties recaptchaProperties;

    @Autowired
    private SlugService slugService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private MailProperties mailProperties;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvitationRepository invitationRepository;

    private AppUser securityAppUser;

    private String slugInstitution;

    private Institution institution;

    @BeforeEach
    public void beforeEach() {
        reset();

        securityAppUser = appUserRepository.save(AppUser.builder()
                .email("get@profile.com")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());

        recaptchaProperties.getTesting().setSuccessResult(true);
        recaptchaProperties.getTesting().setResultErrorCodes(emptyList());


        String test_institution = "Test Institution";
        slugInstitution = slugService.slugify(test_institution);
        institution = institutionRepository.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());

        userRoleRepository.save(UserRole.builder().
                role(AppRole.INST_MEMBER)
                .user(securityAppUser)
                .institution(institution)
                .build());
    }

    @AfterEach
    public void afterEach() {
        reset();
    }

    private void reset() {
        testDbHelper.clean();
    }

    @Component
    private static class TestListener {
        @EventListener
        public void handle(OnRegistrationCompleteEvent event) {
        }

        @EventListener
        public void handle(OnEmailConfirmedEvent event) {
        }

        @EventListener
        public void handle(OnResendRegistrationTokenEvent event) {
        }

        @EventListener
        public void handle(OnRegistrationDuplicateEvent event) {
        }

        @EventListener
        public void handle(OnConfirmInvitationEvent event) {

        }
    }

    @Test
    public void shouldCreateUserAddedToInstitution() throws Exception {
        val invitation = invitationRepository
                .save(InvitationHelper.invitationBuilder(institution).build());
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .password("admin123")
                .domain(AppUser.ScientificDomain.ATMOSPHERE)
                .usage(AppUser.Usage.RESEARCH)
                .country("PL")
                .build();

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()), isEmpty());

        mockMvc.perform(post(API_PREFIX_V1 + "/register?token=" + invitation.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isOk());

        // User should have roles in institution
        val userInstitutionRoles = userRoleRepository
                .findUserRolesInInstitution(
                        registerRequest.getEmail(),
                        invitation.getInstitution().getSlug()

                )
                .stream()
                .map(role -> role.getRole().name())
                .toArray();
        assertThat(userInstitutionRoles, is(new String[]{AppRole.INST_MEMBER.name()}));

        // Should send confirmation email
        verify(testListener).handle(any(OnConfirmInvitationEvent.class));
    }

    @Test
    public void shouldCreateUserOnInvalidToken() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .password("admin123")
                .domain(AppUser.ScientificDomain.ATMOSPHERE)
                .usage(AppUser.Usage.RESEARCH)
                .country("PL")
                .build();

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()), isEmpty());

        mockMvc.perform(post(API_PREFIX_V1 + "/register?token=test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isOk());

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()), isPresent());
    }

    @Test
    public void shouldCreateUser() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .password("admin123")
                .domain(AppUser.ScientificDomain.ATMOSPHERE)
                .usage(AppUser.Usage.RESEARCH)
                .country("PL")
                .build();
        GreenMailUser mailUser = greenMail.setUser("some@email.pl", "");
        MailFolder inbox = getInbox(greenMail, mailUser);

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()), isEmpty());

        mockMvc.perform(post(API_PREFIX_V1 + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isOk());

        // RecaptchaValidator should be called.
        verify(recaptchaValidator).validate(any(HttpServletRequest.class));
        verifyNoMoreInteractions(recaptchaValidator);

        // AppUser should have been created and fields been set.
        val optionalAppUser = appUserRepository.findByEmail(registerRequest.getEmail());
        assertThat(optionalAppUser, isPresent());
        val appUser = optionalAppUser.get();
        assertThat(appUser, allOf(
                hasProperty("name", equalTo("Name")),
                hasProperty("surname", equalTo("Surname")),
                hasProperty("domain", equalTo(AppUser.ScientificDomain.ATMOSPHERE)),
                hasProperty("usage", equalTo(AppUser.Usage.RESEARCH)),
                hasProperty("country", equalTo("PL"))
        ));
        assertThat(passwordEncoder.matches("admin123", appUser.getPassword()), is(true));

        // The OnRegistrationCompleteEvent should be fired.
        verify(testListener).handle(any(OnRegistrationCompleteEvent.class));
        verifyNoMoreInteractions(testListener);

        // Email sending handler is executed in @Async method so allow it to run
        await().atMost(Durations.ONE_SECOND)
                .until(() -> inbox.getMessageCount() == 1);

        // The message should contain a link with the token.
        val messageParser = getParser(inbox.getMessages().get(0).getMimeMessage());
        assertThat(messageParser.getPlainContent(), containsString(mailProperties.getUrlDomain() + "/activate/"));
        assertThat(messageParser.getHtmlContent(), containsString(mailProperties.getUrlDomain() + "/activate/"));
    }

    @Test
    public void shouldntCreateUserIfRecaptchaFails() throws Exception {
        recaptchaProperties.getTesting().setSuccessResult(false);
        recaptchaProperties.getTesting().setResultErrorCodes(List.of(
                ErrorCode.MISSING_USER_CAPTCHA_RESPONSE,
                ErrorCode.INVALID_USER_CAPTCHA_RESPONSE));
        val responseParameter = recaptchaProperties.getValidation().getResponseParameter();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .password("admin123")
                .build();

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()), isEmpty());

        mockMvc.perform(post(API_PREFIX_V1 + "/register")
                .param(responseParameter, "testCaptchaResponse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.recaptcha", hasItems(
                        ErrorCode.MISSING_USER_CAPTCHA_RESPONSE.getText(),
                        ErrorCode.INVALID_USER_CAPTCHA_RESPONSE.getText())));

        // Recaptcha validator should be called passing the response parameter.
        verify(recaptchaValidator).validate(argThat(
                (HttpServletRequest request) ->
                        "testCaptchaResponse".equals(request.getParameter(responseParameter))));
        verifyNoMoreInteractions(recaptchaValidator);

        // No AppUser should have been created.
        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()), isEmpty());
        verifyNoMoreInteractions(testListener);
    }

    @Test
    public void shouldValidateInput() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("notanemail.pl")
                .password("")
                .build();

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()), isEmpty());

        mockMvc.perform(post(API_PREFIX_V1 + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("email.length()", is(equalTo(1))))
                .andExpect(jsonPath("password.length()", is(equalTo(2))));

        // No AppUser should have been created.
        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()), isEmpty());
        verifyNoMoreInteractions(testListener);
    }

    @Test
    public void shouldReturn200EvenIfEmailExistsAndSendNotificationToExistingUser() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .password("admin123")
                .build();
        appUserRepository.save(AppUser.builder()
                .name("Name")
                .surname("Surname")
                .email(registerRequest.getEmail())
                .password("someHash")
                .build());
        GreenMailUser mailUser = greenMail.setUser("some@email.pl", "");
        MailFolder inbox = getInbox(greenMail, mailUser);

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()), isPresent());

        mockMvc.perform(post(API_PREFIX_V1 + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isOk());

        // The account should not have been updated. This means the same password hash.
        AppUser appUser = appUserRepository.findByEmail(registerRequest.getEmail()).get();
        assertThat(appUser.getPassword(), is(equalTo("someHash")));

        // Existing user should be notified that someone tried to register on his account.
        verify(testListener).handle(any(OnRegistrationDuplicateEvent.class));
        verifyNoMoreInteractions(testListener);

        // Email sending handler is executed in @Async method so allow it to run
        await().atMost(Durations.ONE_SECOND)
                .until(() -> inbox.getMessageCount() == 1);

        // The message should contain a link with the token.
        val messageParser = getParser(inbox.getMessages().get(0).getMimeMessage());
        assertThat(messageParser.getPlainContent(), containsString("zresetować"));
        assertThat(messageParser.getHtmlContent(), containsString("zresetować"));
    }

    @Test
    public void shouldActivateUserBasedOnToken() throws Exception {
        AppUser appUser = appUserRepository.save(AppUser.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .enabled(false)
                .build());
        GreenMailUser mailUser = greenMail.setUser("some@email.pl", "");
        MailFolder inbox = getInbox(greenMail, mailUser);

        EmailVerification emailVerification = emailVerificationRepository.save(EmailVerification.builder()
                .appUser(appUser)
                .expiryTimestamp(LocalDateTime.now().plusHours(1))
                .token("theTokenValue")
                .build());

        assertThat(appUser.isEnabled(), is(false));

        mockMvc.perform(post(API_PREFIX_V1 + "/confirm-email")
                .param("token", emailVerification.getToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // The AppUser should have enabled flag updated
        AppUser updatedAppUser = appUserRepository.findById(appUser.getId()).get();
        assertThat(updatedAppUser.isEnabled(), is(true));

        // and OnEmailConfirmedEvent should have been fired.
        verify(testListener).handle(any(OnEmailConfirmedEvent.class));
        verifyNoMoreInteractions(testListener);

        // Email sending handler is executed in @Async method so allow it to run.
        await().atMost(Durations.ONE_SECOND)
                .until(() -> inbox.getMessageCount() == 1);

        // EmailVerification is only removed in the EmailConfirmationListener, so it is verified after
        // the email has been received.
        assertThat(emailVerificationRepository.findById(emailVerification.getId()), isEmpty());

        // Finally, an email with info that the account has been activated should be sent.
        StoredMessage storedMessage = inbox.getMessages().get(0);
        assertThat(getBody(storedMessage.getMimeMessage()), containsString("aktywowane"));
    }

    @Test
    public void shouldntActivateUserBasedOnExpiredToken() throws Exception {
        AppUser appUser = appUserRepository.save(AppUser.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .enabled(false)
                .build());

        EmailVerification emailVerification = emailVerificationRepository.save(EmailVerification.builder()
                .appUser(appUser)
                .expiryTimestamp(LocalDateTime.now().minusHours(1))
                .token("theTokenValue")
                .build());

        assertThat(appUser.isEnabled(), is(false));

        mockMvc.perform(post(API_PREFIX_V1 + "/confirm-email")
                .param("token", emailVerification.getToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // The AppUser shouldn't have enabled flag updated
        AppUser updatedAppUser = appUserRepository.findById(appUser.getId()).get();
        assertThat(updatedAppUser.isEnabled(), is(false));

        // and OnEmailConfirmedEvent should not be fired.
        verifyNoMoreInteractions(testListener);
    }

    @Test
    public void shouldResendTokenByEmail() throws Exception {
        final String EMAIL = "some@email.pl";

        AppUser appUser = appUserRepository.save(AppUser.builder()
                .email(EMAIL)
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .enabled(false)
                .build());

        EmailVerification emailVerification = emailVerificationRepository.save(EmailVerification.builder()
                .appUser(appUser)
                .expiryTimestamp(LocalDateTime.now().plusHours(1))
                .token("theTokenValue")
                .build());

        GreenMailUser mailUser = greenMail.setUser(EMAIL, "");
        MailFolder inbox = getInbox(greenMail, mailUser);

        assertThat(appUser.isEnabled(), is(false));

        mockMvc.perform(post(API_PREFIX_V1 + "/resend-registration-token-by-email")
                .contentType(MediaType.APPLICATION_JSON)
                .param("email", EMAIL))
                .andExpect(status().isOk());

        // OnResendRegistrationTokenEvent should be fired.
        verify(testListener).handle(any(OnResendRegistrationTokenEvent.class));
        verifyNoMoreInteractions(testListener);

        // Email sending handler is executed in @Async method so allow it to run.
        await().atMost(Durations.ONE_SECOND)
                .until(() -> inbox.getMessageCount() == 1);

        // The message should contain a link with the token.
        val messageParser = getParser(inbox.getMessages().get(0).getMimeMessage());
        assertThat(messageParser.getPlainContent(), containsString(mailProperties.getUrlDomain() + "/activate/"));
        assertThat(messageParser.getHtmlContent(), containsString(mailProperties.getUrlDomain() + "/activate/"));

        // The old token should be deleted
        assertThat(emailVerificationRepository.findById(emailVerification.getId()), isEmpty());
        // and new one created.
        assertThat(emailVerificationRepository.findByAppUserId(appUser.getId()), isPresent());
    }

    @Test
    public void shouldntResendTokenIfAppUserNotFound() throws Exception {
        mockMvc.perform(post(API_PREFIX_V1 + "/resend-registration-token-by-email")
                .contentType(MediaType.APPLICATION_JSON)
                .param("email", "some@email.pl"))
                .andExpect(status().isOk());

        // OnResendRegistrationTokenEvent shouldn't be fired.
        verifyNoMoreInteractions(testListener);
    }

    @Test
    public void shouldResendTokenByExistingToken() throws Exception {
        final String EMAIL = "some@email.pl";
        final String TOKEN = "theTokenValue";

        AppUser appUser = appUserRepository.save(AppUser.builder()
                .email(EMAIL)
                .name("Name")
                .surname("Surname")
                .password("someHash")
                .enabled(false)
                .build());

        EmailVerification emailVerification = emailVerificationRepository.save(EmailVerification.builder()
                .appUser(appUser)
                .expiryTimestamp(LocalDateTime.now().plusHours(1))
                .token(TOKEN)
                .build());

        GreenMailUser mailUser = greenMail.setUser(EMAIL, "");
        MailFolder inbox = getInbox(greenMail, mailUser);

        assertThat(appUser.isEnabled(), is(false));

        mockMvc.perform(post(API_PREFIX_V1 + "/resend-registration-token-by-token")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", TOKEN))
                .andExpect(status().isOk());

        // OnResendRegistrationTokenEvent should be fired.
        verify(testListener).handle(any(OnResendRegistrationTokenEvent.class));
        verifyNoMoreInteractions(testListener);

        // Email sending handler is executed in @Async method so allow it to run.
        await().atMost(Durations.ONE_SECOND)
                .until(() -> inbox.getMessageCount() == 1);

        // The message should contain a link with the token.
        val messageParser = getParser(inbox.getMessages().get(0).getMimeMessage());
        assertThat(messageParser.getPlainContent(), containsString(mailProperties.getUrlDomain() + "/activate/"));
        assertThat(messageParser.getHtmlContent(), containsString(mailProperties.getUrlDomain() + "/activate/"));

        // The old token should be deleted
        assertThat(emailVerificationRepository.findById(emailVerification.getId()), isEmpty());
        // and new one created.
        assertThat(emailVerificationRepository.findByAppUserId(appUser.getId()), isPresent());
    }

    @Test
    public void shouldntResendTokenByNonExistingToken() throws Exception {
        final String TOKEN = "theTokenValue";

        mockMvc.perform(post(API_PREFIX_V1 + "/resend-registration-token-by-token")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", TOKEN))
                .andExpect(status().isNotFound());

        // OnResendRegistrationTokenEvent shouldn't be fired.
        verifyNoMoreInteractions(testListener);
    }


    @Test
    public void shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnProfile() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/users/me")
                .with(jwtBearerToken(securityAppUser, objectMapper)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnProfileWithTokenCookie() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1 + "/users/me")
                .with(jwtCookieToken(securityAppUser, objectMapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("get@profile.com"))
                .andExpect(jsonPath("$.name").value("Get"))
                .andExpect(jsonPath("$.surname").value("Profile"))
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.memberZK").value(false))
                .andExpect(jsonPath("$.roles", hasSize(1)));
    }

    public static MailFolder getInbox(GreenMailExtension greenMail, GreenMailUser mailUser) throws FolderException {
        return greenMail.getManagers().getImapHostManager().getInbox(mailUser);
    }

    private MimeMessageParser getParser(MimeMessage mimeMessage) throws Exception {
        return new MimeMessageParser(mimeMessage).parse();
    }
}
