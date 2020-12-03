package pl.cyfronet.s4e.bean;

import lombok.*;
import org.hibernate.annotations.NaturalId;
import pl.cyfronet.s4e.bean.audit.CreationAndModificationAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

/**
 * If you delete Institution, you will also delete all Group entries
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(name = "institution", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "slug"}))
public class Institution extends CreationAndModificationAudited {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotEmpty
    @EqualsAndHashCode.Include
    private String name;

    private String address;

    private String postalCode;

    private String city;

    private String phone;

    private String secondaryPhone;

    @NotEmpty
    @NaturalId(mutable = true)
    @EqualsAndHashCode.Include
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    private Institution parent;

    @OneToMany(mappedBy = "parent")
    @ToString.Exclude
    private Set<Institution> children;

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<UserRole> membersRoles = new HashSet<>();

    public void addMemberRole(UserRole role) {
        membersRoles.add(role);
    }

    public void removeMemberRole(UserRole role) {
        membersRoles.remove(role);
    }

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<LicenseGrant> licenseGrants;

    private boolean eumetsatLicense;

    private boolean zk;

    private boolean pak;
}
