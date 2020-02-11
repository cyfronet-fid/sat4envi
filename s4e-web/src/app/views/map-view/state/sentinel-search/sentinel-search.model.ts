import {EntityState, guid, ID} from '@datorama/akita';

export interface SentinelSearchForm {
  sentinelId: string;
  dateStart: string;
  dateEnd: string;
  clouds: number;
}

export interface SentinelSearchResult {
  id: string;
  image: string,
  mission: string;
  instrument: string;
  sensingDate: string;
  size: string;
  url: string;
}

export interface Sentinel {
    id: string;
    caption: string;
}

/**
 * A factory function that creates SentinelSearchResult
 */
export function createSentinelSearchResult(params: Partial<SentinelSearchResult>) {
  return {
    id: guid(),
    ...params
  } as SentinelSearchResult;
}

export interface SentinelSearchState extends EntityState<SentinelSearchResult> {
  sentinels: Sentinel[];
}
