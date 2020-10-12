package pl.cyfronet.s4e.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.*;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.ex.product.ProductDeletionException;
import pl.cyfronet.s4e.ex.product.ProductException;
import pl.cyfronet.s4e.ex.product.ProductValidationException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@PersistenceContext(type = PersistenceContextType.EXTENDED)
public class ProductService {
    public static final String GRANULE_ARTIFACT_RULE_MISSING_DEFAULT_CODE = "pl.cyfronet.s4e.service.ProductService.granuleArtifactRule.missing-default";
    public static final String SCHEMA_DOESNT_EXIST_CODE = "pl.cyfronet.s4e.service.ProductService.schema.doesnt-exist";
    public static final String SCHEMA_INCORRECT_TYPE_CODE = "pl.cyfronet.s4e.service.ProductService.schema.incorrect-type";

    @Data
    @Builder
    public static class DTO {
        private String name;

        private String displayName;

        private String description;

        private Product.AccessType accessType;

        private Legend legend;

        private String layerName;

        private String sceneSchemaName;

        private String metadataSchemaName;

        private Map<String, String> granuleArtifactRule;

        @Builder.Default
        private String productCategoryName = ProductCategoryRepository.DEFAULT_CATEGORY_NAME;
    }

    private final ProductRepository productRepository;

    private final SceneRepository sceneRepository;

    private final AppUserRepository appUserRepository;

    private final SchemaRepository schemaRepository;

    private final ProductCategoryRepository productCategoryRepository;

    private final ObjectMapper objectMapper;

    private final ProductMapper productMapper;

    public <T> List<T> findAll(Class<T> projection) {
        return productRepository.findAllByOrderByIdAsc(projection);
    }

    public <T> List<T> findAllFetchSchemasAndCategory(Class<T> projection) {
        return productRepository.findAllFetchSchemasAndCategory(projection);
    }

    public <T> Optional<T> findById(Long id, Class<T> projection) {
        return productRepository.findById(id, projection);
    }

    public <T> Optional<T> findByIdFetchSchemasAndCategory(Long id, Class<T> projection) {
        return productRepository.findByIdFetchSchemasAndCategory(id, projection);
    }

    public <T> List<T> findAllFetchProductCategory(Class<T> projection) {
        return productRepository.findAllFetchProductCategory(Sort.by("id"), projection);
    }

    @Transactional
    public Long create(DTO dto) throws ProductException, NotFoundException {
        val productBuilder = Product.builder();

        BindingResult bindingResult = new MapBindingResult(objectMapper.convertValue(dto, Map.class), "productCreateRequest");
        productBuilder.sceneSchema(findAndValidateSchema(
                dto.getSceneSchemaName(), Schema.Type.SCENE, "sceneSchemaName", bindingResult));
        productBuilder.metadataSchema(findAndValidateSchema(
                dto.getMetadataSchemaName(), Schema.Type.METADATA, "metadataSchemaName", bindingResult));
        if (!dto.getGranuleArtifactRule().containsKey("default")) {
            bindingResult.rejectValue("granuleArtifactRule", GRANULE_ARTIFACT_RULE_MISSING_DEFAULT_CODE);
        }

        val productCategory = productCategoryRepository
                .findByName(dto.getProductCategoryName(), ProductCategory.class)
                .orElseThrow(() -> constructNFE("Product category", dto.getProductCategoryName()));
        productBuilder.productCategory(productCategory);

        if (bindingResult.hasErrors()) {
            throw new ProductValidationException(bindingResult);
        }

        productMapper.create(dto, productBuilder);
        return productRepository.save(productBuilder.build()).getId();
    }

    @Transactional
    public void update(Long id, DTO dto) throws ProductException, NotFoundException {
        Product product = productRepository.findById(id)
                .orElseThrow((() -> constructNFE("Product", id.toString())));

        if (
                dto.getProductCategoryName() != null
                && !product.getProductCategory()
                        .getName()
                        .contains(dto.getProductCategoryName())
        ) {
            val productCategory = productCategoryRepository
                    .findByName(dto.getProductCategoryName(), ProductCategory.class)
                    .orElseThrow(() -> constructNFE("Product category", dto.getProductCategoryName()));
            product.setProductCategory(productCategory);
        }

        BindingResult bindingResult = new MapBindingResult(objectMapper.convertValue(dto, Map.class), "productUpdateRequest");
        Schema sceneSchema = null;
        if (dto.getSceneSchemaName() != null) {
            sceneSchema = findAndValidateSchema(dto.getSceneSchemaName(), Schema.Type.SCENE, "sceneSchemaName", bindingResult);
        }
        Schema metadataSchema = null;
        if (dto.getMetadataSchemaName() != null) {
            metadataSchema = findAndValidateSchema(dto.getMetadataSchemaName(), Schema.Type.METADATA, "metadataSchemaName", bindingResult);
        }
        if (dto.getGranuleArtifactRule() != null && !dto.getGranuleArtifactRule().containsKey("default")) {
            bindingResult.rejectValue("granuleArtifactRule", GRANULE_ARTIFACT_RULE_MISSING_DEFAULT_CODE);
        }

        if (bindingResult.hasErrors()) {
            throw new ProductValidationException(bindingResult);
        }

        productMapper.update(dto, product);

        if (product.getAccessType() != Product.AccessType.PRIVATE) {
            product.getLicenseGrants().clear();
        }

        if (sceneSchema != null) {
            product.setSceneSchema(sceneSchema);
        }

        if (metadataSchema != null) {
            product.setMetadataSchema(metadataSchema);
        }
    }

    private Schema findAndValidateSchema(String schemaName, Schema.Type type, String fieldName, BindingResult bindingResult) {
        Optional<Schema> optionalSceneSchema = schemaRepository.findByName(schemaName);
        if (optionalSceneSchema.isEmpty()) {
            bindingResult.rejectValue(fieldName, SCHEMA_DOESNT_EXIST_CODE);
            return null;
        } else {
            Schema sceneSchema = optionalSceneSchema.get();
            if (type != sceneSchema.getType()) {
                bindingResult.rejectValue(fieldName, SCHEMA_INCORRECT_TYPE_CODE);
            }
            return sceneSchema;
        }
    }

    @Transactional
    public void delete(Long id) throws NotFoundException, ProductDeletionException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> constructNFE("Product", id.toString()));
        long sceneCount = sceneRepository.count(Example.of(Scene.builder().product(product).build()));
        if (sceneCount > 0) {
            throw new ProductDeletionException("Found " + sceneCount + " existing Scenes for this Product. Remove them before proceeding");
        }
        productRepository.deleteById(id);
    }

    public boolean isFavourite(Long id) {
        AppUserDetails userDetails = AppUserDetailsSupplier.get();
        if (userDetails != null) {
            return productRepository.isFavouriteByEmailAndProductId(userDetails.getUsername(), id);
        }
        return false;
    }

    @Transactional
    public void addFavourite(Long id, String username) throws NotFoundException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> constructNFE("Product", id.toString()));

        AppUser appUser = appUserRepository.findByEmail(username)
                .orElseThrow(() -> constructNFE("AppUser", username));

        product.addFavourite(appUser);
    }

    @Transactional
    public void deleteFavourite(Long id, String username) throws NotFoundException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> constructNFE("Product", id.toString()));

        AppUser appUser = appUserRepository.findByEmail(username)
                .orElseThrow(() -> constructNFE("AppUser", username));

        product.removeFavourite(appUser);
    }

    private NotFoundException constructNFE(String name, String id) {
        return new NotFoundException(name + " with id '" + id + "' not found");
    }
}
