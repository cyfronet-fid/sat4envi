package pl.cyfronet.s4e.bean;

import lombok.*;
import org.hibernate.annotations.Type;
import pl.cyfronet.s4e.bean.audit.CreationAndModificationAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * If you delete user, you will also delete password_reset and email_verification entries
 */
@Entity

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class AppUser extends CreationAndModificationAudited {
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<UserRole> roles = new HashSet<>();

    public void addRole(UserRole role) {
        roles.add(role);
    }

    public void removeRole(UserRole role) {
        roles.remove(role);
    }

    private boolean enabled;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "authority")
    @Singular
    private Set<String> authorities;

    public boolean addAuthority(String authority) {
        val mutableAuthorities = new HashSet<>(authorities);
        val out = mutableAuthorities.add(authority);
        authorities = mutableAuthorities.stream().collect(Collectors.toUnmodifiableSet());
        return out;
    }

    public boolean removeAuthority(String authority) {
        val mutableAuthorities = new HashSet<>(authorities);
        val out = mutableAuthorities.remove(authority);
        authorities = mutableAuthorities.stream().collect(Collectors.toUnmodifiableSet());
        return out;
    }

    @Enumerated(EnumType.STRING)
    private ScientificDomain domain;

    @Enumerated(EnumType.STRING)
    private Usage usage;

    private String country;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    @ToString.Exclude
    private UserPreferences preferences = UserPreferences.builder()
            .nonVisibleOverlays(List.of())
            .build();

    public enum ScientificDomain {
        ATMOSPHERE, EMERGENCY, MARINE, LAND, SECURITY, CLIMATE, OTHER
    }

    public enum Usage {
        RESEARCH, COMMERCIAL, EDUCATION, OTHER
    }
}

