import * as Factory from 'factory.ts';
import {Product} from './product.model';

export const ProductFactory = Factory.makeFactory<Product>({
  id: Factory.each(i => i),
  productTypeId: Factory.each(i => i + 1000),
  timestamp: Factory.each(i => '2019-05-01T00:00:00T'),
  layerName: Factory.each(i => `layer #${i}`),
  legend: null
});
