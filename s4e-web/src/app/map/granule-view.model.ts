import {Granule} from '../products/granule.model';
import {Product} from '../products/product.model';

export interface GranuleView {
  product: Product;
  granule: Granule | undefined;
}
