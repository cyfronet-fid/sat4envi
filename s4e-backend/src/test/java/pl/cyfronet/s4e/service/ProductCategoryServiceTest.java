/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
