import {Injectable} from '@angular/core';
import {EntityStore, StoreConfig} from '@datorama/akita';
import {SentinelSearchResult, SentinelSearchState} from './sentinel-search.model';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'SentinelSearchResult' })
export class SentinelSearchStore extends EntityStore<SentinelSearchState, SentinelSearchResult> {

  constructor() {
    super({
      sentinels: [],
      loading: false
    });
  }

}

