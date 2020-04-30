package pl.cyfronet.s4e.bean;

import lombok.*;
import pl.cyfronet.s4e.bean.audit.CreationAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "user_role", uniqueConstraints = @UniqueConstraint(columnNames = {"role", "app_user_id", "inst_group_id"}))
public class UserRole extends CreationAudited {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @EqualsAndHashCode.Include
    private AppRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Include
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inst_group_id")
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Include
    private Group group;

}
