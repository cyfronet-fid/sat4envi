package pl.cyfronet.s4e.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * There is ON CASCADE DELETE on Appuser
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordReset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private AppUser appUser;

    @NotEmpty
    private String token;

    @NotNull
    private LocalDateTime expiryTimestamp;
}
