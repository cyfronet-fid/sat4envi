package pl.cyfronet.s4e.controller.response;

import org.springframework.beans.factory.annotation.Value;
import pl.cyfronet.s4e.bean.AppRole;

public interface UserRoleResponse {

    AppRole getRole();

    @Value("#{target.group.slug}")
    String getGroupSlug();

    @Value("#{target.group.institution.slug}")
    String getInstitutionSlug();
}
