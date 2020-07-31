package pl.cyfronet.s4e.admin.product;

import org.mapstruct.Mapper;
import pl.cyfronet.s4e.config.MapStructCentralConfig;
import pl.cyfronet.s4e.service.ProductService;

@Mapper(config = MapStructCentralConfig.class)
public interface AdminProductMapper {
    ProductService.DTO toProductServiceDTO(AdminCreateProductRequest request);
    ProductService.DTO toProductServiceDTO(AdminUpdateProductRequest request);
}
