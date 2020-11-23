package pl.cyfronet.s4e.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {
    @Getter
    private final long id;

    @Getter
    private final String email;

    @Getter
    private final String name;

    @Getter
    private final String surname;

    @Getter
    private final Set<SimpleGrantedAuthority> authorities;

    @Getter
    private final String password;

    @Getter
    private final boolean enabled;

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

}
