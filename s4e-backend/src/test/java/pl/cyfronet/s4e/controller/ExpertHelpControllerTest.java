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
import lombok.val;
import org.apache.commons.mail.util.MimeMessageParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.InvitationHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.controller.request.ExpertHelpRequest;
import pl.cyfronet.s4e.controller.request.HelpType;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.PropertyRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;

import javax.mail.internet.MimeMessage;

import static com.icegreen.greenmail.configuration.GreenMailConfiguration.aConfig;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class ExpertHelpControllerTest {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(aConfig().withDisabledAuthentication());

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDbHelper testDbHelper;

    private AppUser nonZkMember;
    private AppUser zkMember;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        nonZkMember = appUserRepository.save(InvitationHelper.userBuilder().build());
        zkMember = appUserRepository.save(InvitationHelper.userBuilder().build());

        val institution = institutionRepository.save(Institution.builder()
                .name("ZK")
                .slug("zk")
                .zk(true)
                .build());

        userRoleRepository.save(UserRole.builder()
                .institution(institution)
                .user(zkMember)
                .role(AppRole.INST_MEMBER)
                .build());
    }

    @Nested
    class WithConfiguration {
        @BeforeEach
        public void beforeEach() {
            propertyRepository.save(Property.builder()
                    .name(Constants.PROPERTY_EXPERT_HELP_EMAIL)
                    .value("some@expert.pl")
                    .build());
        }

        @Test
        public void shouldSendEmails() throws Exception {
            ExpertHelpRequest request = ExpertHelpRequest.builder()
                    .helpType(HelpType.AT_LOCATION)
                    .issueDescription("some description")
                    .build();

            GreenMailUser zkMailUser = greenMail.setUser(zkMember.getEmail(), "");
            MailFolder zkInbox = getInbox(greenMail, zkMailUser);
            GreenMailUser expertMailUser = greenMail.setUser("some@expert.pl", "");
            MailFolder expertInbox = getInbox(greenMail, expertMailUser);

            mockMvc.perform(post(API_PREFIX_V1 + "/expert-help")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(zkMember, objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isOk());

            await().until(() -> zkInbox.getMessageCount() == 1 && expertInbox.getMessageCount() == 1);

            {
                val messageParser = getParser(zkInbox.getMessages().get(0).getMimeMessage());
                assertThat(messageParser.getPlainContent(), containsString("some description"));
                assertThat(messageParser.getHtmlContent(), containsString("some description"));
            }

            {
                val messageParser = getParser(expertInbox.getMessages().get(0).getMimeMessage());
                assertThat(messageParser.getSubject(), containsString(zkMember.getEmail()));
                assertThat(messageParser.getReplyTo(), is(equalTo(zkMember.getEmail())));
                assertThat(messageParser.getPlainContent(), containsString("some description"));
                assertThat(messageParser.getHtmlContent(), containsString("some description"));
            }
        }
    }

    @Test
    public void shouldReturn500IfMisconfigured() throws Exception {
        ExpertHelpRequest request = ExpertHelpRequest.builder()
                .helpType(HelpType.AT_LOCATION)
                .issueDescription("some description")
                .build();

        try {
            mockMvc.perform(post(API_PREFIX_V1 + "/expert-help")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(jwtBearerToken(zkMember, objectMapper))
                    .content(objectMapper.writeValueAsBytes(request)));
            fail("Should throw");
        } catch (NestedServletException e) {
            assertThat(e.getCause(), is(instanceOf(IllegalStateException.class)));
        }
    }

    @Test
    public void sendHelpRequestShouldBeSecured() throws Exception {
        val URL = API_PREFIX_V1 + "/expert-help";

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(zkMember, objectMapper))
        ).andExpect(status().isBadRequest());

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(nonZkMember, objectMapper))
        ).andExpect(status().isForbidden());

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    public static MailFolder getInbox(GreenMailExtension greenMail, GreenMailUser mailUser) throws FolderException {
        return greenMail.getManagers().getImapHostManager().getInbox(mailUser);
    }

    private MimeMessageParser getParser(MimeMessage mimeMessage) throws Exception {
        return new MimeMessageParser(mimeMessage).parse();
    }
}
