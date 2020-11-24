import {environment} from '../../../../../environments/environment';
import {ActiveState, EntityState} from '@datorama/akita';
import {SentinelSearchMetadata} from './sentinel-search.metadata.model';

export const SENTINEL_VISIBLE_QUERY_KEY = 'visible_sent';
export const SENTINEL_SELECTED_QUERY_KEY = 'selected_sent';
export const SENTINEL_THUMBNAIL_ARTIFACT_NAME = 'thumbnail';
export const SENTINEL_SHOW_RESULTS_QUERY_KEY = 'showSearchResults';
export const SENTINEL_SEARCH_PARAMS_QUERY_KEY = 'searchParams';
export const SENTINEL_SEARCH_ACTIVE_ID = 'search_active_id';

export interface SentinelSearchResultMetadata {
  format: string;
  ingestion_time: string;
  polarisation: string;
  polygon: string;
  processing_level: string;
  product_type: string;
  relative_orbit_number: string;
  schema: string;
  sensing_time: string;
  sensor_mode: string;
  spacecraft: string;

  [key: string]: string;
}

/**
 * This is result returned from the server
 */
export interface SentinelSearchResultResponse {
  id: string;
  productId: number;
  timestamp: string;
  artifacts: string[];
  footprint: string;
  sceneKey: string;
  metadataContent: SentinelSearchResultMetadata;
}

export interface SentinelSearchResult extends SentinelSearchResultResponse {
  thumbnailStyle: string;
  sceneKeyShort: string;
  image: string;
  mission: string;
  instrument: string;
  timestamp: string;
  artifactsWithLinks: {artifact: string, url: string}[];
  // this should be calculated from the response
  url: string;
}

export function artifactDownloadLink(id: string, artifact: string): string {
  return `${environment.apiPrefixV1}/scenes/${id}/download/${artifact}`;
}


/**
 * A factory function that creates SentinelSearchResult
 */
export function createSentinelSearchResult(params: SentinelSearchResultResponse) {
  const image = params.artifacts.includes(SENTINEL_THUMBNAIL_ARTIFACT_NAME)
    ? artifactDownloadLink(params.id, SENTINEL_THUMBNAIL_ARTIFACT_NAME)
    : null;

  return {
    image: image,
    sceneKeyShort: params.sceneKey.match(/([^/]+)\.scene$/)[1],
    url: `${environment.apiPrefixV1}/dhus/odata/v1/Products('${params.id}')/$value`,
    metadataContent: {},
    artifacts: [],
    footprint: '',
    artifactsWithLinks: (params.artifacts || []).map(artifact => ({artifact, url: artifactDownloadLink(params.id, artifact)})),
    ...params
  } as SentinelSearchResult;
}

export interface SentinelSearchState extends EntityState<SentinelSearchResult>, ActiveState {
  metadata: SentinelSearchMetadata;
  metadataLoading: boolean;
  metadataLoaded: boolean;
  loaded: boolean;
  showSearchResults: boolean;

  hoveredId: string | number | null;
}
