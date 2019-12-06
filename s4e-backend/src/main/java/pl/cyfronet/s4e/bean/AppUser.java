package pl.cyfronet.s4e.bean;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

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

    @Singular
    private Set<AppRole> roles;

    @ManyToMany(mappedBy = "members", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Group> groups = new HashSet<>();

    private boolean enabled;
}
