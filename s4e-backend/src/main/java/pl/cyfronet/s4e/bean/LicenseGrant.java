package pl.cyfronet.s4e.bean;

import lombok.*;
import pl.cyfronet.s4e.bean.audit.CreationAndModificationAudited;

import javax.persistence.*;

@Entity

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LicenseGrant extends CreationAndModificationAudited {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Institution institution;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    private boolean owner;
}
