package pl.cyfronet.s4e.controller.response;

import pl.cyfronet.s4e.bean.InvitationStatus;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public interface BasicInvitationResponse {
    public String getEmail();

    public String getToken();

    @Enumerated(EnumType.STRING)
    public InvitationStatus getStatus();
}
