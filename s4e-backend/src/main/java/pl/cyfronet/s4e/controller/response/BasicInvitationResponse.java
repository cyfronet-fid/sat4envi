package pl.cyfronet.s4e.controller.response;

import pl.cyfronet.s4e.bean.InvitationStatus;

public interface BasicInvitationResponse {
    public String getEmail();

    public String getToken();

    public InvitationStatus getStatus();
}
