import {JsonObject, JsonProperty} from 'json2typescript';
import {DateConverter} from '../../../../utils/date-converter/date-converter';

export interface Product {
  id: number;
  productTypeId: number;
  timestamp: Date;
  layerName: string;
}

@JsonObject
export class ProductResponse implements Product {
  @JsonProperty('id', Number)
  id: number = undefined;

  @JsonProperty('productTypeId', Number)
  productTypeId: number = undefined;

  @JsonProperty('timestamp', DateConverter)
  timestamp: Date = undefined;

  @JsonProperty('layerName', String)
  layerName: string = undefined;
}

/**
 * A factory function that creates Product
 */
export function createProduct(params: Partial<Product>) {
  return {

  } as Product;
}
