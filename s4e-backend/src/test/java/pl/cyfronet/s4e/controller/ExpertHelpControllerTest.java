package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.InvitationHelper;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.UserRoleRepository;
import pl.cyfronet.s4e.service.SlugService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class ExpertHelpControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private SlugService slugService;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private AppUser nonZkMember;
    private AppUser zkMember;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        nonZkMember = appUserRepository.save(InvitationHelper.userBuilder().build());
        zkMember = appUserRepository.save(
                InvitationHelper.userBuilder()
                        .memberZK(true)
                        .build()
        );
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
}
