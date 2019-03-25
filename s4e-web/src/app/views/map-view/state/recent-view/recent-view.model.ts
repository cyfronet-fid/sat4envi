import {Product} from '../product/product.model';
import {ProductType} from '../product-type/product-type.model';

export interface RecentView {
  productId: number|null;
  productTypeId: number;
}

/**
 * A factory function that creates RecentView
 */
export function createRecentView(params: Partial<RecentView>) {
  return {
    productId: null,
    ...params
  } as RecentView;
}

/**
 * Used as a product of the query, not serialized
 */
export interface ICompleteRecentView extends RecentView {
  activeProduct: Product|null;
  activeProductType: ProductType;
}
