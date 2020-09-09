package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.properties.FileStorageProperties;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {
    private final static String ICON_EXTENSION_WITH_DOT = ".svg";

    private final FileStorageProperties fileStorageProperties;

    public String getIconPath(String iconName) {
        return String.join(
                "/",
                fileStorageProperties.getBucket(),
                fileStorageProperties.getKeyPrefixProductsCategoriesIcons(),
                iconName,
                ICON_EXTENSION_WITH_DOT
        );
    }
}
