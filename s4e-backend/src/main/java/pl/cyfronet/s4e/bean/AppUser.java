package pl.cyfronet.s4e.bean;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Entity
@Data
@Builder
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotEmpty
    private String email;
    /// password hash
    @NotEmpty
    private String password;

    @Singular
    private Set<AppRole> roles;

    private boolean enabled;
}
