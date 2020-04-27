package pl.cyfronet.s4e.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.TestResourceHelper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@BasicTest
@Slf4j
@AutoConfigureMockMvc
class JwtControllerTest {
    public static final String PUBLIC_KEY_PATH = "classpath:jwt/dev_key.pub";

    @Autowired
    private TestResourceHelper testResourceHelper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldWork() throws Exception {
        String actualPublicKey = new String(testResourceHelper.getAsBytes(PUBLIC_KEY_PATH));

        mockMvc.perform(get(API_PREFIX_V1 + "/jwt/pubkey")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(actualPublicKey));
    }
}
