/*
 * Copyright 2021 ACC Cyfronet AGH
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

import * as Factory from 'factory.ts';
import {Product, ProductCategory} from './product.model';

export const ProductCategoryFactory = Factory.makeFactory<ProductCategory>({
  id: Factory.each(i => i),
  label: Factory.each(i => `Product category #${i}`),
  iconPath: Factory.each(i => `http://test.com/#${i}`),
  rank: Factory.each(i => i)
});

export const ProductFactory = Factory.makeFactory<Product>({
  id: Factory.each(i => i),
  name: Factory.each(i => `Product #${i}`),
  displayName: Factory.each(i => `Product #${i}`),
  imageUrl: Factory.each(i => `/assets/product-type-logo/${i}.png`),
  description: '',
  layerName: Factory.each(i => `layer #${i}`),
  legend: null,
  favourite: false,
  productCategory: ProductCategoryFactory.build(),
  rank: Factory.each(i => i)
});
