import {UIProductCategory} from './product/product.model';

export interface IUILayer {
  cid: number;
  label: string;
  active: boolean;
  favourite: boolean;
  isLoading: boolean;
  isFavouriteLoading: boolean;

  category?: UIProductCategory;
}
