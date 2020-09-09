import * as Factory from 'factory.ts';
import { Product, ProductCategory } from './product.model';

const ProductCategoryFactory = Factory.makeFactory<ProductCategory>({
  id: Factory.each(i => i),
  label: Factory.each(i => `Product category #${i}`),
  iconPath: Factory.each(i => `http://test.com/#${i}`),
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
  productCategory: ProductCategoryFactory.build()
});
