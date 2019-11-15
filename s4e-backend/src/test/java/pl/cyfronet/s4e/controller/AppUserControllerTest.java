package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkopylec.recaptcha.RecaptchaProperties;
import com.github.mkopylec.recaptcha.validation.ErrorCode;
import com.github.mkopylec.recaptcha.validation.RecaptchaValidator;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.StoredMessage;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.GreenMailSupplier;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.EmailVerification;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.controller.request.CreateUserWithGroupsRequest;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.EmailVerificationRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.event.OnEmailConfirmedEvent;
import pl.cyfronet.s4e.event.OnRegistrationCompleteEvent;
import pl.cyfronet.s4e.event.OnResendRegistrationTokenEvent;
import pl.cyfronet.s4e.service.GroupService;
import pl.cyfronet.s4e.service.InstitutionService;
import pl.cyfronet.s4e.service.SlugService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.util.Collections.emptyList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class AppUserControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SlugService slugService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private GroupService groupService;

    private GreenMail greenMail;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        emailVerificationRepository.deleteAll();
        institutionRepository.deleteAll();
        appUserRepository.deleteAll();

        appUserRepository.save(AppUser.builder()
                .email("get@profile.com")
                .name("Get")
                .surname("Profile")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build());

        greenMail = new GreenMailSupplier().get();
        greenMail.start();

        recaptchaProperties.getTesting().setSuccessResult(true);
        recaptchaProperties.getTesting().setResultErrorCodes(emptyList());
    }

    @AfterEach
    public void afterEach() {
        greenMail.stop();
    }

    @Component
    private static class TestListener {
        @EventListener
        public void handle(OnRegistrationCompleteEvent event) { }

        @EventListener
        public void handle(OnEmailConfirmedEvent event) { }

        @EventListener
        public void handle(OnResendRegistrationTokenEvent event) { }
    }

    @Test
    public void shouldCreateUser() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("some@email.pl")
                .name("Name")
                .surname("Surname")
                .password("admin123")
                .build();
        GreenMailUser mailUser = greenMail.setUser("some@email.pl", "");
        MailFolder inbox = getInbox(greenMail, mailUser);

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(false));

        mockMvc.perform(post(API_PREFIX_V1+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isOk());

        // RecaptchaValidator should be called.
        verify(recaptchaValidator).validate(any(HttpServletRequest.class));
        verifyNoMoreInteractions(recaptchaValidator);

        // AppUser should have been created and the OnRegistrationCompleteEvent fired.
        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(true));
        verify(testListener).handle(any(OnRegistrationCompleteEvent.class));
        verifyNoMoreInteractions(testListener);

        // Email sending handler is executed in @Async method so allow it to run
        await().atMost(Durations.ONE_SECOND)
                .until(() -> inbox.getMessageCount() == 1);

        // The message should contain a link with the token.
        StoredMessage storedMessage = inbox.getMessages().get(0);
        assertThat(getBody(storedMessage.getMimeMessage()), containsString("localhost/activate/"));
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

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(false));

        mockMvc.perform(post(API_PREFIX_V1+"/register")
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
        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(false));
        verifyNoMoreInteractions(testListener);
    }

    @Test
    public void shouldValidateInput() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("notanemail.pl")
                .password("")
                .build();

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(false));

        mockMvc.perform(post(API_PREFIX_V1+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(mvcResult -> {
                    log.info(mvcResult.getResponse().getContentAsString());
                })
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("email.length()", is(equalTo(1))))
                .andExpect(jsonPath("password.length()", is(equalTo(2))));

        // No AppUser should have been created.
        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(false));
        verifyNoMoreInteractions(testListener);
    }

    @Test
    public void shouldReturn200EvenIfEmailExists() throws Exception {
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

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(true));

        mockMvc.perform(post(API_PREFIX_V1+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isOk());

        // The account should not have been updated. This means the same password hash and no event fired.
        AppUser appUser = appUserRepository.findByEmail(registerRequest.getEmail()).get();
        assertThat(appUser.getPassword(), is(equalTo("someHash")));
        verifyNoMoreInteractions(testListener);
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

        mockMvc.perform(post(API_PREFIX_V1+"/confirm-email")
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
        assertThat(emailVerificationRepository.findById(emailVerification.getId()).isPresent(), is(false));

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

        mockMvc.perform(post(API_PREFIX_V1+"/confirm-email")
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

        mockMvc.perform(post(API_PREFIX_V1+"/resend-registration-token-by-email")
                .param("email", EMAIL))
                .andExpect(status().isOk());

        // OnResendRegistrationTokenEvent should be fired.
        verify(testListener).handle(any(OnResendRegistrationTokenEvent.class));
        verifyNoMoreInteractions(testListener);

        // Email sending handler is executed in @Async method so allow it to run.
        await().atMost(Durations.ONE_SECOND)
                .until(() -> inbox.getMessageCount() == 1);

        // The message should contain a link with the token.
        StoredMessage storedMessage = inbox.getMessages().get(0);
        assertThat(getBody(storedMessage.getMimeMessage()), containsString("localhost/activate/"));

        // The old token should be deleted
        assertThat(emailVerificationRepository.findById(emailVerification.getId()).isPresent(), is(false));
        // and new one created.
        assertThat(emailVerificationRepository.findByAppUserId(appUser.getId()).isPresent(), is(true));
    }

    @Test
    public void shouldntResendTokenIfAppUserNotFound() throws Exception {
        mockMvc.perform(post(API_PREFIX_V1+"/resend-registration-token-by-email")
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

        mockMvc.perform(post(API_PREFIX_V1+"/resend-registration-token-by-token")
                .param("token", TOKEN))
                .andExpect(status().isOk());

        // OnResendRegistrationTokenEvent should be fired.
        verify(testListener).handle(any(OnResendRegistrationTokenEvent.class));
        verifyNoMoreInteractions(testListener);

        // Email sending handler is executed in @Async method so allow it to run.
        await().atMost(Durations.ONE_SECOND)
                .until(() -> inbox.getMessageCount() == 1);

        // The message should contain a link with the token.
        StoredMessage storedMessage = inbox.getMessages().get(0);
        assertThat(getBody(storedMessage.getMimeMessage()), containsString("localhost/activate/"));

        // The old token should be deleted
        assertThat(emailVerificationRepository.findById(emailVerification.getId()).isPresent(), is(false));
        // and new one created.
        assertThat(emailVerificationRepository.findByAppUserId(appUser.getId()).isPresent(), is(true));
    }

    @Test
    public void shouldntResendTokenByNonExistingToken() throws Exception {
        final String TOKEN = "theTokenValue";

        mockMvc.perform(post(API_PREFIX_V1+"/resend-registration-token-by-token")
                .param("token", TOKEN))
                .andExpect(status().isNotFound());

        // OnResendRegistrationTokenEvent shouldn't be fired.
        verifyNoMoreInteractions(testListener);
    }


    @Test
    public void shouldReturnForbiddenCode403() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1+"/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("get@profile.com")
    public void shouldReturnProfile() throws Exception {
        mockMvc.perform(get(API_PREFIX_V1+"/users/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void shouldCreateUserAndAddToGroup() throws Exception {
        String test_institution = "Test Institution";
        String slugInstitution = slugService.slugify(test_institution);
        institutionService.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());

        Set<String> groups = new HashSet<>();
        groups.add("default");
        CreateUserWithGroupsRequest createUserWithGroupsRequest = CreateUserWithGroupsRequest.builder()
                .name("Name")
                .surname("Surname")
                .email("email@test.pl")
                .groupSlugs(groups)
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/institutions/{institution}/users", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createUserWithGroupsRequest)))
                .andExpect(status().isOk());

        assertThat(appUserRepository.findByEmail("email@test.pl").isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution,"default"), hasSize(1));
    }

    @Test
    @WithMockUser
    public void shouldCreateUserWithoutAddingtoGroup() throws Exception {
        String test_institution = "Test Institution";
        String slugInstitution = slugService.slugify(test_institution);
        institutionService.save(Institution.builder()
                .name(test_institution)
                .slug(slugInstitution)
                .build());

        CreateUserWithGroupsRequest createUserWithGroupsRequest = CreateUserWithGroupsRequest.builder()
                .name("Name")
                .surname("Surname")
                .email("email@test.pl")
                .build();

        mockMvc.perform(post(API_PREFIX_V1+"/institutions/{institution}/users", slugInstitution)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createUserWithGroupsRequest)))
                .andExpect(status().isOk());

        assertThat(appUserRepository.findByEmail("email@test.pl").isPresent(), is(true));
        assertThat(groupService.getMembers(slugInstitution,"default"), hasSize(0));
    }

    public static MailFolder getInbox(GreenMail greenMail, GreenMailUser mailUser) throws FolderException {
        return greenMail.getManagers().getImapHostManager().getInbox(mailUser);
    }
}
