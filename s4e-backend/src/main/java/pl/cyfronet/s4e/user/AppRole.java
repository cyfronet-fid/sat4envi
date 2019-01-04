package pl.cyfronet.s4e.user;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableSet;

@Getter
public enum AppRole {
    CAT1("ROLE_CAT1"),
    CAT2("ROLE_CAT2"),
    CAT3("ROLE_CAT3"),
    CAT4("ROLE_CAT4");

    private final Set<SimpleGrantedAuthority> authorities;

    AppRole(String... authoritiesStrings) {
        this.authorities = unmodifiableSet(toSGASet(authoritiesStrings));
    }

    private static Set<SimpleGrantedAuthority> toSGASet(String[] authoritiesStrings) {
        return Arrays.stream(authoritiesStrings)
                .map(auth -> new SimpleGrantedAuthority(auth))
                .collect(Collectors.toSet());
    }
}
