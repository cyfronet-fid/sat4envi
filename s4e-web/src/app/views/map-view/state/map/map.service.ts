import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MapStore} from './map.store';
import {S4eConfig} from '../../../../utils/initializer/config.service';

@Injectable({providedIn: 'root'})
export class MapService {

  constructor(private mapStore: MapStore,
              private http: HttpClient,
              private CONFIG: S4eConfig
  ) {
  }
}
