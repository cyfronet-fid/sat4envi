import {Injectable} from '@angular/core';
import {EntityStore, StoreConfig} from '@datorama/akita';
import {SentinelSearchResult, SentinelSearchState} from './sentinel-search.model';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'SentinelSearchResult' })
export class SentinelSearchStore extends EntityStore<SentinelSearchState, SentinelSearchResult> {
  constructor() {
    super({
      loading: false,
      loaded: false,
      metadataLoading: false,
      metadataLoaded: false,
      resultPagesCount: null,
      resultTotalCount: null,
      metadata: {
        common: {
          params: []
        },
        sections: []
      },
      footprint: null
    });
  }

  setMetadataLoading(loading: boolean = true) {
    this.update({metadataLoading: loading});
  }

  setMetadataLoaded(loaded: boolean = true) {
    this.update({metadataLoading: false, metadataLoaded: loaded});
  }

  setLoaded(loaded: boolean = true) {
    this.update({loaded})
  }
}

