package pl.cyfronet.s4e.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.config.MapStructCentralConfig;

@Mapper(
        config = MapStructCentralConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sceneSchema", ignore = true)
    @Mapping(target = "metadataSchema", ignore = true)
    @Mapping(target = "scenes", ignore = true)
    @Mapping(target = "favourites", ignore = true)
    @Mapping(target = "productCategory", ignore = true)
    void create(ProductService.DTO dto, @MappingTarget Product.ProductBuilder productBuilder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sceneSchema", ignore = true)
    @Mapping(target = "metadataSchema", ignore = true)
    @Mapping(target = "scenes", ignore = true)
    @Mapping(target = "favourites", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "productCategory", ignore = true)
    void update(ProductService.DTO dto, @MappingTarget Product product);
}
