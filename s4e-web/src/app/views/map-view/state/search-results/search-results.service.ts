import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SearchResultsStore} from './search-results.store';
import {SearchResult} from './search-result.model';
import {delay, finalize, map} from 'rxjs/operators';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {AkitaGuidService} from './guid.service';
import {PageSearchResult} from '../../../../utils/state.types';
import {MapService} from '../map/map.service';
import proj4 from 'proj4';
import {ViewPosition} from '../map/map.model';

@Injectable({providedIn: 'root'})
export class SearchResultsService {

  constructor(private store: SearchResultsStore,
              private guidGenerationService: AkitaGuidService,
              private mapService: MapService,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }


  get(placePrefix: string) {
    this.store.update({isOpen: placePrefix.length > 0, queryString: placePrefix});

    if (placePrefix.length === 0) {
      return;
    }

    this.store.setLoading(true);
    this.http.get<PageSearchResult>(`${this.CONFIG.apiPrefixV1}/places`, {params: {namePrefix: placePrefix}}).pipe(
      delay(100),
      map(data => data.content.map(r => ({...r, id: this.guidGenerationService.guid()}))),
      finalize(() => {
          this.store.setLoading(false);
        }
      ),
    ).subscribe(data => this.store.set(data));
  }


  private getZoomLevel(type: string): number | null {
    const ZOOM_LEVELS = {
      'miasto': 10,
      'wieś': 12
    };
    return ZOOM_LEVELS[type] || null;
  }

  setSelectedPlace(searchResult: SearchResult | null) {
    this.mapService.setView({
      centerCoordinates: proj4(this.CONFIG.projection.toProjection, [searchResult.longitude, searchResult.latitude]),
      zoomLevel: this.getZoomLevel(searchResult.type)
    } as ViewPosition);
    this.store.update({selectedLocation: searchResult, isOpen: false});
  }
}
