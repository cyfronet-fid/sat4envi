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

package pl.cyfronet.s4e;

import lombok.val;
import pl.cyfronet.s4e.bean.ProductCategory;

import java.util.concurrent.atomic.AtomicInteger;

public class ProductCategoryHelper {
    private static final AtomicInteger COUNT = new AtomicInteger();

    private static final String label = "Product category %d";
    private static final String iconName = "icon %d SVG";

    public static ProductCategory.ProductCategoryBuilder productCategoryBuilder() {
        val label = nextUnique(ProductCategoryHelper.label);
        val iconName = nextUnique(ProductCategoryHelper.iconName);
        return ProductCategory
                .builder()
                .label(label)
                .name(label)
                .iconName(iconName);
    }

    private static String nextUnique(String format) {
        return String.format(format, COUNT.getAndIncrement());
    }
}
