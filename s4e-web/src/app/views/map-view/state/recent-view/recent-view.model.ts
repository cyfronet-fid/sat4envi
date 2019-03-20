import {Granule} from '../granule/granule.model';
import {Product} from '../product/product.model';

export interface RecentView {
  granuleId: number|null;
  productId: number;
}

/**
 * A factory function that creates RecentView
 */
export function createRecentView(params: Partial<RecentView>) {
  return {
    granuleId: null,
    ...params
  } as RecentView;
}

/**
 * Used as a product of the query, not serialized
 */
export interface ICompleteRecentView extends RecentView {
  activeGranule: Granule|null;
  activeProduct: Product;
}
