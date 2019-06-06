package pl.cyfronet.s4e.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@BasicTest
@Slf4j
public class AppUserControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        appUserRepository.deleteAll();
    }

    @Test
    public void shouldCreateUser() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("some@email.pl")
                .password("admin123")
                .build();

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(false));

        mockMvc.perform(post(API_PREFIX_V1+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isOk());

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(true));
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

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(false));
    }

    @Test
    public void shouldReturn200EvenIfEmailExists() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("some@email.pl")
                .password("admin123")
                .build();
        appUserRepository.save(AppUser.builder()
                .email(registerRequest.getEmail())
                .password("someHash")
                .build());

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(true));

        mockMvc.perform(post(API_PREFIX_V1+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registerRequest)))
                .andExpect(status().isOk());

        assertThat(appUserRepository.findByEmail(registerRequest.getEmail()).isPresent(), is(true));
    }
}
