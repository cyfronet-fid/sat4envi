package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.properties.FileStorageProperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@BasicTest
@Slf4j
public class ProductCategoryServiceTest {
    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Test
    public void shouldReturnUrlToCategoriesFileInS3() {
        val iconName = "test";
        val iconNameWithExtension = iconName + ProductCategoryService.ICON_EXTENSION_WITH_DOT;
        val bucketWithIconsKey = "/static-test/" + fileStorageProperties.getKeyPrefixProductsCategoriesIcons();
        val iconPath = bucketWithIconsKey + iconNameWithExtension;

        assertThat(productCategoryService.getIconPath(iconName), is(iconPath));
    }
}
