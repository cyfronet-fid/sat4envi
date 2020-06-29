import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { SentinelSearchStore} from './sentinel-search.store';
import {SentinelSearchResult, SentinelSearchState} from './sentinel-search.model';

@Injectable({
  providedIn: 'root'
})
export class SentinelSearchQuery extends QueryEntity<SentinelSearchState, SentinelSearchResult> {

  constructor(protected store: SentinelSearchStore) {
    super(store);
  }

  selectSentinels() {
    return this.select('sentinels');
  }
}
