package pl.cyfronet.s4e.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Immutable

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneExtended {
    @Id
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    private LocalDateTime timestamp;

    private Geometry footprint;

    private String s3Path;

    private String granulePath;
}
