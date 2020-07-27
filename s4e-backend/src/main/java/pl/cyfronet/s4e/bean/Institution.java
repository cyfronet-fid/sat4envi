package pl.cyfronet.s4e.bean;

import lombok.*;
import org.hibernate.annotations.NaturalId;
import pl.cyfronet.s4e.bean.audit.CreationAndModificationAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

/**
 * If you delete Institution, you will also delete all Group entries
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "institution", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "slug"}))
public class Institution extends CreationAndModificationAudited {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    private String address;

    private String postalCode;

    private String city;

    private String phone;

    private String secondaryPhone;

    @NotEmpty
    @NaturalId(mutable = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Institution parent;
}
