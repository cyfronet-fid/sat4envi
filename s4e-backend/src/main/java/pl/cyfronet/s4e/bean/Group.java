package pl.cyfronet.s4e.bean;

import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

/**
 * There is ON CASCADE DELETE on Institution
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "inst_group", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "institution_id"}),
        @UniqueConstraint(columnNames = {"slug", "institution_id"})})
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotEmpty
    @EqualsAndHashCode.Include
    private String name;

    @NotEmpty
    @NaturalId(mutable = true)
    @EqualsAndHashCode.Include
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    @ToString.Exclude
    private Institution institution;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<UserRole> membersRoles = new HashSet<>();

    public void removeMemberRole(UserRole role) {
        membersRoles.remove(role);
    }
}
