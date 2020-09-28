package pl.cyfronet.s4e.config;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import pl.cyfronet.s4e.security.JwtAuthenticationFilter;

import java.security.SecureRandom;
import java.util.Arrays;

import static org.springframework.http.HttpMethod.*;
import static pl.cyfronet.s4e.Constants.ADMIN_PREFIX;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    public SecurityConfig() {
        // disable the defaults
        super(true);
    }

    private static String[] prefix(String... paths) {
        return Arrays.stream(paths)
                .map(path -> API_PREFIX_V1 + path)
                .toArray(n -> new String[n]);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .mvcMatcher("/api/**")
                .anonymous(anonymous -> {})
                .exceptionHandling(handling -> handling
                                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeRequests(this::authorizeRequests)
                .addFilterBefore(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class);
    }

    private void authorizeRequests(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorize) {
        authorize
                .mvcMatchers(POST, prefix(
                        "/token",
                        "/login",
                        "/logout"
                )).permitAll()

                .mvcMatchers(GET, prefix("/users/me")).authenticated()
                .mvcMatchers(POST, prefix(
                        "/register",
                        "/resend-registration-token-by-email",
                        "/resend-registration-token-by-token",
                        "/confirm-email"
                )).permitAll()

                .mvcMatchers(POST, prefix("/password-change")).authenticated()
                .mvcMatchers(POST, prefix("/token-create", "/password-reset")).permitAll()
                .mvcMatchers(GET, prefix("/token-validate")).permitAll()

                .mvcMatchers(GET, prefix("/config", "/config/sentinel-search")).permitAll()

                .mvcMatchers(GET, prefix("/jwt/pubkey")).permitAll()

                .mvcMatchers(POST, prefix("/invitations/{token}/confirm")).authenticated()
                .mvcMatchers(PUT, prefix("/invitations/{token}/reject")).permitAll()

                .mvcMatchers(GET, prefix("/overlays")).permitAll()
                .mvcMatchers(POST, prefix("/overlays/personal")).authenticated()
                .mvcMatchers(POST, prefix("/overlays/global"))
                    .access("hasRole('ADMIN')")
                .mvcMatchers(DELETE, prefix("/overlays/personal/{id}")).authenticated()
                .mvcMatchers(DELETE, prefix("/overlays/global/{id}"))
                    .access("hasRole('ADMIN')")
                .mvcMatchers(PUT, prefix("/overlays/{id}/visible")).authenticated()
                .mvcMatchers(DELETE, prefix("/overlays/{id}/visible")).authenticated()

                .mvcMatchers(GET, prefix("/institutions/{institution}"))
                    .access("hasRole('ADMIN') || @ish.isMember(#institution)")
                .mvcMatchers(DELETE, prefix("/institutions/{institution}"))
                    .access("hasRole('ADMIN')")
                .mvcMatchers(prefix("/institutions/{institution}", "/institutions/{institution}/**"))
                    .access("hasRole('ADMIN') || @ish.isAdmin(#institution)")
                .mvcMatchers(GET, prefix("/institutions")).authenticated()
                .mvcMatchers(prefix("/institutions")).hasRole("ADMIN")

                .mvcMatchers(prefix("/user-role")).hasRole("ADMIN")

                .mvcMatchers(POST, prefix("/saved-views")).authenticated()
                .mvcMatchers(GET, prefix("/saved-views")).authenticated()
                .mvcMatchers(DELETE, prefix("/saved-views/{uuid}"))
                    .access("isAuthenticated() && @savedViewService.canDelete(#uuid, authentication)")

                .mvcMatchers(POST, prefix("/share-link")).hasRole("MEMBER_ZK")

                .mvcMatchers(GET, prefix("/schemas", "/schemas/{name}")).permitAll()

                .mvcMatchers(GET, prefix("/products", "/products/{id}")).permitAll()
                .mvcMatchers(PUT, prefix("/products/{id}/favourite")).authenticated()
                .mvcMatchers(DELETE, prefix("/products/{id}/favourite")).authenticated()

                .mvcMatchers(GET, prefix(
                        "/products/{id}/scenes",
                        "/products/{id}/scenes/available",
                        "/products/{id}/scenes/most-recent",
                        "/scenes/{id}/download"
                )).permitAll()

                .mvcMatchers(GET, prefix("/search")).permitAll()
                .mvcMatchers(GET, prefix("/dhus/**")).authenticated()

                .mvcMatchers(GET, prefix("/overlays/prg", "/overlays/wms")).permitAll()

                .mvcMatchers(GET, prefix("/places")).permitAll()

                .mvcMatchers(ADMIN_PREFIX + "/**").hasRole("ADMIN")

                .anyRequest().denyAll();
    }

    @Bean
    public InstitutionSecurityHelper ish() {
        return new InstitutionSecurityHelper();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        val dap = new DaoAuthenticationProvider();
        dap.setPasswordEncoder(passwordEncoder());
        dap.setUserDetailsService(userDetailsService);
        return dap;
    }

    @Override
    protected UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
