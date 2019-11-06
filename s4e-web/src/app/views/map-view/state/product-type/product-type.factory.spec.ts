import * as Factory from 'factory.ts';
import {ProductType} from './product-type.model';

export const ProductTypeFactory = Factory.makeFactory<ProductType>({
  id: Factory.each(i => i),
  name: Factory.each(i => `Product Type #${i}`),
  imageUrl: Factory.each(i => `/assets/product-type-logo/${i}.png`),
  description: '',
  productIds: undefined,
  legend: null
});
