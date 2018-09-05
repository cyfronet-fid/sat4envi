import {Layer as olLayer} from 'ol/layer';

import {Product} from '../products/product.model';

export interface Layer {
  product: Product;
  olLayer: olLayer;
}
