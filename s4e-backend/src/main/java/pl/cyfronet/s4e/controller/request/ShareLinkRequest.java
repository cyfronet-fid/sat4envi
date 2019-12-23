package pl.cyfronet.s4e.controller.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
public class ShareLinkRequest {
    @NotEmpty
    private String link;
    @NotEmpty
    private List<@Email String> emails;
}
