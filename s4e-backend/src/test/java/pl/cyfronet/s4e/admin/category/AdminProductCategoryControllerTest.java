package pl.cyfronet.s4e.admin.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.Constants;
import pl.cyfronet.s4e.TestDbHelper;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductCategoryRepository;

import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.cyfronet.s4e.TestJwtUtil.jwtBearerToken;

@BasicTest
@AutoConfigureMockMvc
public class AdminProductCategoryControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDbHelper testDbHelper;

    private AppUser admin;
    private AppUser user;

    @BeforeEach
    public void beforeEach() {
        testDbHelper.clean();

        admin = appUserRepository.save(AppUser.builder()
                .email("admin@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .admin(true)
                .build());

        user = appUserRepository.save(AppUser.builder()
                .email("user@mail.pl")
                .name("John")
                .surname("Smith")
                .password("{noop}password")
                .enabled(true)
                .build());
    }

    @Test
    public void shouldBeSecured() throws Exception {
        val url = Constants.ADMIN_PREFIX + "/product-category/initiate";
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(user, objectMapper))
                .content(objectMapper.writeValueAsBytes("{}")))
                .andExpect(status().isForbidden()
        );
    }

    @Test
    public void shouldInitializeDB() throws Exception {
        val url = Constants.ADMIN_PREFIX + "/product-category/seed";
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtBearerToken(admin, objectMapper))
                .content(objectMapper.writeValueAsBytes("{}")))
                .andExpect(status().isOk()
        );

        val productCategories = productCategoryRepository.findAll();
        assertThat(StreamSupport.stream(productCategories.spliterator(), false).count(), is(greaterThanOrEqualTo(1L)));
    }
}
