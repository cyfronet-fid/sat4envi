package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.properties.FileStorageProperties;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {
    public final static String ICON_EXTENSION_WITH_DOT = ".svg";

    private final FileStorageProperties fileStorageProperties;

    public String getIconPath(String iconName) {
        return fileStorageProperties.getPathPrefix() +
                fileStorageProperties.getKeyPrefixProductsCategoriesIcons() +
                iconName + ICON_EXTENSION_WITH_DOT;
    }
}
