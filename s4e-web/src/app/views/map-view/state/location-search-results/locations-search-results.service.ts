/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LocationSearchResultsStore} from './locations-search-results.store';
import {LocationSearchResult} from './location-search-result.model';
import {delay, finalize, map} from 'rxjs/operators';
import {PageSearchResult} from '../../../../utils/state.types';
import {MapService} from '../map/map.service';
import proj4 from 'proj4';
import {ViewPosition} from '../map/map.model';
import {AkitaGuidService} from '../search-results/guid.service';
import {LocationSearchResultsQuery} from './location-search-results.query';
import environment from 'src/environments/environment';

@Injectable({providedIn: 'root'})
export class SearchResultsService {
  constructor(
    private store: LocationSearchResultsStore,
    private query: LocationSearchResultsQuery,
    private guidGenerationService: AkitaGuidService,
    private mapService: MapService,
    private http: HttpClient
  ) {}

  get(placePrefix: string) {
    this.store.update({isOpen: placePrefix.length > 0, queryString: placePrefix});

    if (placePrefix.length === 0) {
      return;
    }

    this.store.setLoading(true);
    this.http
      .get<PageSearchResult>(`${environment.apiPrefixV1}/places`, {
        params: {namePrefix: placePrefix}
      })
      .pipe(
        delay(100),
        map(data =>
          data.content.map(r => ({...r, id: this.guidGenerationService.guid()}))
        ),
        finalize(() => {
          this.store.setLoading(false);
        })
      )
      .subscribe(data => this.store.set(data));
  }

  toggleSearchResults(show = true) {
    this.store.update({isOpen: show});
  }

  private getZoomLevel(type: string): number | null {
    const ZOOM_LEVELS = {
      miasto: 10,
      wieś: 12
    };
    return ZOOM_LEVELS[type] || null;
  }

  setSelectedPlace(searchResult: LocationSearchResult | null) {
    if (!!searchResult) {
      this.mapService.setView({
        centerCoordinates: proj4(environment.projection.toProjection, [
          searchResult.longitude,
          searchResult.latitude
        ]),
        zoomLevel: this.getZoomLevel(searchResult.type)
      } as ViewPosition);
    }
    this.store.update({searchResult: searchResult, isOpen: false});
  }
}
