package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.mail.util.MimeMessageParser;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.*;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.ShareLinkRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import javax.mail.internet.MimeMessage;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class ShareLinkControllerTest {
    private static final String IMAGE_PNG_PATH = "classpath:images/image.png";

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestResourceHelper testResourceHelper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private MailProperties mailProperties;

    private AppUser appUser;

    private GreenMail greenMail;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        appUser = appUserRepository.save(AppUser.builder()
                .email("get@profile.com")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .memberZK(true)
                .build());

        greenMail = new GreenMailSupplier().get();
        greenMail.start();
    }

    @AfterEach
    public void afterEach() {
        greenMail.stop();
        testDbHelper.clean();
    }

    @Test
    public void shouldShareLink() throws Exception {
        val request = ShareLinkRequest.builder()
                .caption("Some caption")
                .description("Yet another description")
                .path("/some/path?hehe=tralala")
                .thumbnail(testResourceHelper.getAsStringInBase64(IMAGE_PNG_PATH))
                .emails(List.of("some-1@email.pl", "some-2@email.pl"))
                .build();

        MailFolder inbox1 = getInbox(greenMail, greenMail.setUser("some-1@email.pl", ""));
        MailFolder inbox2 = getInbox(greenMail, greenMail.setUser("some-2@email.pl", ""));

        mockMvc.perform(post(API_PREFIX_V1 + "/share-link")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isOk());

        // Email sending handler is executed in @Async method so allow it to run
        await().atMost(Durations.ONE_SECOND)
                .until(() -> inbox1.getMessageCount() == 1 && inbox2.getMessageCount() == 1);

        MimeMessageParser parser1 = getParserForFirstMail(inbox1);
        assertThat(parser1.getPlainContent(), containsString(request.getCaption()));
        assertThat(parser1.getPlainContent(), containsString(request.getDescription()));
        assertThat(parser1.getPlainContent(), containsString(mailProperties.getUrlDomain() + request.getPath()));
        assertThat(parser1.getHtmlContent(), containsString(mailProperties.getUrlDomain() + request.getPath()));
        assertThat(parser1.getAttachmentList().size(), is(equalTo(1)));
        assertThat(parser1.getAttachmentList().get(0).getContentType(), is(equalTo("image/png")));

        MimeMessageParser parser2 = getParserForFirstMail(inbox2);
        assertThat(parser2.getPlainContent(), containsString(request.getCaption()));
        assertThat(parser2.getPlainContent(), containsString(request.getDescription()));
        assertThat(parser2.getPlainContent(), containsString(mailProperties.getUrlDomain() + request.getPath()));
        assertThat(parser2.getHtmlContent(), containsString(mailProperties.getUrlDomain() + request.getPath()));
        assertThat(parser2.getAttachmentList().size(), is(equalTo(1)));
        assertThat(parser2.getAttachmentList().get(0).getContentType(), is(equalTo("image/png")));
    }

    public static MimeMessageParser getParserForFirstMail(MailFolder inbox) throws Exception {
        MimeMessage mimeMessage = inbox.getMessages().get(0).getMimeMessage();
        return new MimeMessageParser(mimeMessage).parse();
    }

    public static MailFolder getInbox(GreenMail greenMail, GreenMailUser mailUser) throws FolderException {
        return greenMail.getManagers().getImapHostManager().getInbox(mailUser);
    }
}
