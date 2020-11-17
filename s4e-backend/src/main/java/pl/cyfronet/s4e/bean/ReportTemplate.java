package pl.cyfronet.s4e.bean;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import pl.cyfronet.s4e.bean.audit.CreationAudited;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@TypeDef(name = "list-array", typeClass = ListArrayType.class)

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportTemplate extends CreationAudited {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AppUser owner;

    private String caption;

    private String notes;

    @Type(type = "list-array")
    @Builder.Default
    private List<Long> overlayIds = List.of();

    private Long productId;
}
