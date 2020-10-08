import {ID} from '@datorama/akita';
import {ProductCategory, UIProductCategory} from './product/product.model';

export interface IUILayer {
  cid: ID;
  label: string;
  active: boolean;
  favourite: boolean;
  isLoading: boolean;
  isFavouriteLoading: boolean;

  category?: UIProductCategory;
}
