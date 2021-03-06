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

package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.ServerSetupTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.mail.util.MimeMessageParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.TestResourceHelper;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.bean.UserRole;
import pl.cyfronet.s4e.controller.request.ShareLinkRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.properties.MailProperties;

import javax.mail.internet.MimeMessage;
import java.util.List;

import static com.icegreen.greenmail.configuration.GreenMailConfiguration.aConfig;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@AutoConfigureMockMvc
@BasicTest
@Slf4j
public class ShareLinkControllerTest {
    private static final String IMAGE_PNG_PATH = "classpath:images/image.png";

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

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        appUser = appUserRepository.save(AppUser.builder()
                .email("get@profile.com")
                .name("Get")
                .surname("Profile")
                .password("{noop}password")
                .enabled(true)
                .build());

        val institution = institutionRepository.save(Institution.builder()
                .name("ZK")
                .slug("zk")
                .zk(true)
                .build());

        userRoleRepository.save(UserRole.builder()
                .institution(institution)
                .user(appUser)
                .role(AppRole.INST_MEMBER)
                .build());
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
        await().until(() -> inbox1.getMessageCount() == 1 && inbox2.getMessageCount() == 1);

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

    @Test
    public void shouldValidateThumbnail() throws Exception {
        val request = ShareLinkRequest.builder()
                .caption("Some caption")
                .description("Yet another description")
                .path("/some/path?hehe=tralala")
                .thumbnail("bad thumbnail format")
                .emails(List.of("some-1@email.pl", "some-2@email.pl"))
                .build();

        mockMvc.perform(post(API_PREFIX_V1 + "/share-link")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(jwtBearerToken(appUser, objectMapper)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thumbnail", hasSize(3)));
    }

    public static MimeMessageParser getParserForFirstMail(MailFolder inbox) throws Exception {
        MimeMessage mimeMessage = inbox.getMessages().get(0).getMimeMessage();
        return new MimeMessageParser(mimeMessage).parse();
    }

    public static MailFolder getInbox(GreenMailExtension greenMail, GreenMailUser mailUser) throws FolderException {
        return greenMail.getManagers().getImapHostManager().getInbox(mailUser);
    }
}
