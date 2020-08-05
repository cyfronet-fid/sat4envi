import { environment } from './../../../../../environments/environment';
import {ActiveState, EntityState} from '@datorama/akita';
import {SentinelSearchMetadata} from './sentinel-search.metadata.model';

export const SENTINEL_VISIBLE_QUERY_KEY = 'visible_sent';
export const SENTINEL_SELECTED_QUERY_KEY = 'selected_sent';

/**
 * This is result returned from the server
 */
export interface SentinelSearchResultResponse {
  id: string;
  productId: number;
  timestamp: string;
}

export interface SentinelSearchResult extends SentinelSearchResultResponse {
  image: string;
  mission: string;
  instrument: string;
  size: string;
  // this should be calculated from the response
  url: string;
}

/**
 * A factory function that creates SentinelSearchResult
 */
export function createSentinelSearchResult(params: SentinelSearchResultResponse) {
  return {
    image: null,
    mission: 'Sentinel-1',
    instrument: 'SAR-C',
    size: '6.97 GB',
    url: `${environment.apiPrefixV1}/dhus/odata/v1/Products('${params.productId}')/$value`,
    ...params
  } as SentinelSearchResult;
}

export interface SentinelSearchState extends EntityState<SentinelSearchResult>, ActiveState {
  metadata: SentinelSearchMetadata;
  metadataLoading: boolean;
  metadataLoaded: boolean;
  loaded: boolean;
}
