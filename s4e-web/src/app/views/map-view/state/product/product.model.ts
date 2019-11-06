import {JsonObject, JsonProperty} from 'json2typescript';
import {DateConverter} from '../../../../utils/date-converter/date-converter';
import {Legend} from '../legend/legend.model';

export interface Product {
  id: number;
  productTypeId: number;
  timestamp: string;
  layerName: string;
  legend: Legend|null;
}

/**
 * A factory function that creates Product
 */
export function createProduct(params: Partial<Product>) {
  return {

  } as Product;
}
