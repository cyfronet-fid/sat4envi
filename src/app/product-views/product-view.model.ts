import {Product} from '../products/product.model';

export interface ProductView {
  id: string;
  layers: Array<Product>;
}
