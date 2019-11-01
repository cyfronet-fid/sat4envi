import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SearchResultsStore} from './search-results.store';
import {PageSearchResult} from './search-result.model';
import {delay, finalize, map} from 'rxjs/operators';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {AkitaGuidService} from './guid.service';

@Injectable({providedIn: 'root'})
export class SearchResultsService {

  constructor(private searchResultsStore: SearchResultsStore,
              private guidGenerationService: AkitaGuidService,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }


  get(placePrefix: string) {
    this.searchResultsStore.update({isOpen: placePrefix.length > 0, queryString: placePrefix});

    if (placePrefix.length === 0) {
      return;
    }

    this.searchResultsStore.setLoading(true);
    this.http.get<PageSearchResult>(`${this.CONFIG.apiPrefixV1}/places`, {params: {namePrefix: placePrefix}}).pipe(
      delay(100),
      map(data => data.content.map(r => ({...r, id: this.guidGenerationService.guid()}))),
      finalize(() => {
          this.searchResultsStore.setLoading(false);
        }
      ),
    ).subscribe(data => this.searchResultsStore.set(data));
  }
}
