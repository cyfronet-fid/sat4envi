package pl.cyfronet.s4e.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.AppRole;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.RefreshTokenRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@BasicTest
class PersistingJwtTokenStoreTest {
    private static final String USER_ID = "test@test.pl";
    private static final String USER_PASSWORD = "testPassword";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach() {
        refreshTokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    public void testRefreshTokenRevocationFlow() throws Exception {
        // Create the test user.
        appUserRepository.save(AppUser.builder()
                .email(USER_ID)
                .password(passwordEncoder.encode(USER_PASSWORD))
                .role(AppRole.CAT1)
                .enabled(true)
                .build());

        assertThat(refreshTokenRepository.count(), is(equalTo(0L)));

        // Authorize, get refresh token.
        val retToken = mockMvc.perform(post("/oauth/token")
                .param("grant_type", "password")
                .param("username", USER_ID)
                .param("password", USER_PASSWORD)
                .header("Authorization", httpBasicCredentials("s4e", "secret")))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(refreshTokenRepository.count(), is(equalTo(1L)));

        String refreshToken = objectMapper.readTree(retToken.getResponse().getContentAsString())
                .get("refresh_token").asText();

        // Check the refresh token works.

        mockMvc.perform(post("/oauth/token")
                .param("grant_type", "refresh_token")
                .param("refresh_token", refreshToken)
                .header("Authorization", httpBasicCredentials("s4e", "secret")))
                .andExpect(status().isOk());

        assertThat(refreshTokenRepository.count(), is(equalTo(1L)));

        // Remove the refresh token from db.
        refreshTokenRepository.deleteAll();

        // Check refresh token is revoked.
        mockMvc.perform(post("/oauth/token")
                .param("grant_type", "refresh_token")
                .param("refresh_token", refreshToken)
                .header("Authorization", httpBasicCredentials("s4e", "secret")))
                .andExpect(status().isBadRequest());
    }

    private String httpBasicCredentials(String userName, String password) {
        String headerValue = "Basic ";
        byte[] toEncode = (userName + ":" + password).getBytes(StandardCharsets.UTF_8);
        headerValue += new String(Base64.getEncoder().encode(toEncode), StandardCharsets.UTF_8);
        return headerValue;
    }
}
