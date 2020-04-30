package pl.cyfronet.s4e.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.cyfronet.s4e.bean.audit.CreationAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schema extends CreationAudited {
    public enum Type {
        SCENE, METADATA;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Type type;

    @NotEmpty
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Schema previous;
}
