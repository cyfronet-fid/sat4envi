package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.bean.AppUser;
import pl.cyfronet.s4e.bean.ReportTemplate;
import pl.cyfronet.s4e.config.MapStructCentralConfig;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

@Mapper(config = MapStructCentralConfig.class)
@Slf4j
public abstract class ReportTemplateMapper {
    @Autowired
    private AppUserRepository appUserRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", source = "ownerEmail")
    public abstract ReportTemplate dtoToPreEntity(ReportTemplateService.CreateDTO createDTO) throws NotFoundException;

    protected AppUser ownerEmailToAppUser(String ownerEmail) throws NotFoundException {
        return appUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("AppUser with email '" + ownerEmail + "' not found"));
    }
}
