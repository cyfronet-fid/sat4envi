package pl.cyfronet.s4e.bean;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
@Table(name = "user_role",uniqueConstraints = @UniqueConstraint(columnNames = {"role", "app_user_id", "inst_group_id"}))
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AppRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    @NotNull
    @ToString.Exclude
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inst_group_id")
    @NotNull
    @ToString.Exclude
    private Group group;

}
