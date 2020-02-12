package pl.cyfronet.s4e.bean;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

/**
 * If you delete user, you will also delete password_reset and email_verification entries
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @NotEmpty
    @Column(unique = true)
    @EqualsAndHashCode.Include
    private String email;

    @NotEmpty
    private String name;

    @NotEmpty
    private String surname;

    /// password hash
    @NotEmpty
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<UserRole> roles = new HashSet<>();

    private boolean enabled;

    @Column(name = "member_zk")
    private boolean memberZK;

    private boolean admin;

    public void removeRole(UserRole role) {
        roles.remove(role);
    }
}

