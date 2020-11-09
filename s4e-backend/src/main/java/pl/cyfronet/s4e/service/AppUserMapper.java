package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.config.MapStructCentralConfig;
import pl.cyfronet.s4e.controller.AppUserController;
import pl.cyfronet.s4e.controller.request.RegisterRequest;
import pl.cyfronet.s4e.controller.response.UserMeResponse;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

@Mapper(config = MapStructCentralConfig.class)
@Slf4j
public abstract class AppUserMapper {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", qualifiedByName = "password")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "admin", ignore = true)
    @Mapping(target = "eumetsatLicense", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    public abstract AppUser requestToPreEntity(RegisterRequest registerRequest);

    @Named("password")
    protected String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Mapping(target = "memberZK", source = "name", qualifiedByName = "memberZK")
    public abstract UserMeResponse projectionToMeResponse(AppUserController.UserMeProjection projection);

    @Named("memberZK")
    protected boolean memberZK(String name) {
        return AppUserDetailsSupplier.get().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEMBER_ZK"));
    }
}
