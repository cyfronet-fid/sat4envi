import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MapStore} from './map.store';
import {MapQuery} from './map.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {ViewPosition} from './map.model';

@Injectable({providedIn: 'root'})
export class MapService {

  constructor(private mapStore: MapStore,
              private mapQuery: MapQuery,
              private http: HttpClient,
              private CONFIG: S4eConfig
  ) {
  }

  toggleZKOptions(open: boolean = true) {
    this.mapStore.update({zkOptionsOpened: open});
  }

  setView(view: ViewPosition): void {
    this.mapStore.update({view});
  }
}
