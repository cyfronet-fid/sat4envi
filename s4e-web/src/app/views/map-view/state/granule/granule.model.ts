import { ID } from '@datorama/akita';
import {JsonObject, JsonProperty} from 'json2typescript';
import {DateConverter} from '../../../../utils/date-converter/date-converter';

export interface Granule {
  id: number;
  productId: number;
  timestamp: Date;
  layerName: string;
}

@JsonObject
export class GranuleResponse implements Granule {
  @JsonProperty('id', Number)
  id: number = undefined;

  @JsonProperty('productId', Number)
  productId: number = undefined;

  @JsonProperty('timestamp', DateConverter)
  timestamp: Date = undefined;

  @JsonProperty('layerName', String)
  layerName: string = undefined;
}

/**
 * A factory function that creates Granule
 */
export function createGranule(params: Partial<Granule>) {
  return {

  } as Granule;
}
