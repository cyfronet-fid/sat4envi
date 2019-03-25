import {Inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MapStore } from './map.store';
import {IConstants, S4E_CONSTANTS} from '../../../../app.constants';

@Injectable({ providedIn: 'root' })
export class MapService {

  constructor(private mapStore: MapStore,
              private http: HttpClient,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants) {
  }
}
