package pl.cyfronet.s4e.controller.response;

import pl.cyfronet.s4e.bean.InvitationStatus;

public interface BasicInvitationResponse {
    public Long getId();

    public String getEmail();

    public InvitationStatus getStatus();
}
