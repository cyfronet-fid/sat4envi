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
        size: '6.97 GB'
      }),
      createSentinelSearchResult({
        image: null,
        mission: 'Sentinel-1',
        instrument: 'SAR-C',
        sensingDate: '2019-12-05T18:27:29',
        size: '6.97 GB'
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
