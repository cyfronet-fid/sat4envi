package pl.cyfronet.s4e;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class GlobalSecurityConfig extends GlobalAuthenticationConfigurerAdapter {
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        // Necessary to set it here by hand. Otherwise you get
        // "java.lang.IllegalStateException: UserDetailsService is required."
        // See https://stackoverflow.com/a/56627072, where this is explained.
        auth.userDetailsService(userDetailsService);
    }
}
