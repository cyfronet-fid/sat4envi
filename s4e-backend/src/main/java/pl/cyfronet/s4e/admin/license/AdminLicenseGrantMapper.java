package pl.cyfronet.s4e.admin.license;

import org.mapstruct.Mapper;
import pl.cyfronet.s4e.config.MapStructCentralConfig;

@Mapper(config = MapStructCentralConfig.class)
public interface AdminLicenseGrantMapper {
    LicenseGrantService.CreateDTO toCreateDTO(AdminCreateLicenseGrantRequest request);
}
