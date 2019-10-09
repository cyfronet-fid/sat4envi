package pl.cyfronet.s4e.bean;

import lombok.*;
import org.hibernate.annotations.NaturalId;

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
    private Institution institution;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "inst_group_app_users", joinColumns = @JoinColumn(name = "inst_group_id"), inverseJoinColumns = @JoinColumn(name = "app_user_id"),
            uniqueConstraints = {@UniqueConstraint(
                    columnNames = {"inst_group_id", "app_user_id"})})
    private Set<AppUser> members = new HashSet<>();

    public void addMember(AppUser user) {
        members.add(user);
    }

    public void removeMember(AppUser user) {
        members.removeIf(m -> m.getId() == user.getId());
    }
}
