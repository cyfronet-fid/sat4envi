import { Injectable } from '@angular/core';
import { ID } from '@datorama/akita';
import { HttpClient } from '@angular/common/http';
import { SentinelSearchStore } from './sentinel-search.store';
import {createSentinelSearchResult, SentinelSearchResult} from './sentinel-search.model';
import {Observable, of} from 'rxjs';
import {SentinelSearchQuery} from './sentinel-search.query';
import {delay, finalize} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class SentinelSearchService {

  constructor(private store: SentinelSearchStore,
              private query: SentinelSearchQuery,
              private http: HttpClient) {
  }

  search() {
    this.store.setLoading(true);
    of([
      createSentinelSearchResult({
        image: null,
        mission: 'Sentinel-1',
        instrument: 'SAR-C',
        sensingDate: '2019-12-05T18:27:29',
        size: '6.97 GB',
        url: 'http://s4e-sentinel-training.storage.cloud.cyfronet.pl/S1B_IW_GRDH_1SDV_20200210T045115_20200210T045144_020202_0263F2_0D45.SAFE.zip'
      }),
      createSentinelSearchResult({
        image: null,
        mission: 'Sentinel-1',
        instrument: 'SAR-C',
        sensingDate: '2019-12-05T18:27:29',
        size: '6.97 GB',
        url: 'http://s4e-sentinel-training.storage.cloud.cyfronet.pl/S1B_IW_GRDH_1SDV_20200210T045115_20200210T045144_020202_0263F2_0D45.SAFE.zip'
      })
    ]).pipe(delay(1500), finalize(() => this.store.setLoading(false))).subscribe(
      data => this.store.set(data),
      error => this.store.setError(error)
    )
  }

  getSentinels() {
    of([
      {id: 'sentinel-1', caption: 'Sentinel #1'},
      {id: 'sentinel-2', caption: 'Sentinel #2'},
    ]).pipe(delay(250)).subscribe(
      data => this.store.update(state => ({...state, sentinels: data})),
      error => this.store.setError(error)
    )
  }
}
