import {Injectable} from '@angular/core';
import { Tile, Image } from 'ol/layer';
import { ImageWMS, OSM } from 'ol/source';
import {BehaviorSubject, Observable} from 'rxjs';

import {geoserverUrl} from '../constants';
import {Layer} from './layer.model';

@Injectable({
  providedIn: 'root',
})
export class LayersService {
  private layers: Layer[];
  private subject: BehaviorSubject<Layer[]>;

  constructor() {
    this.layers = [{
      product: {
        id: null,
        timestamp: null,
        type: 'base',
        layerName: 'OpenStreetMap',
      },
      olLayer: new Tile({
        source: new OSM(),
      }),
    }, {
      product: {
        id: null,
        timestamp: null,
        type: 'overlay',
        layerName: 'województwa',
      },
      olLayer: new Image({
        source: new ImageWMS({
          url: geoserverUrl,
          params: { 'LAYERS': 'test:wojewodztwa' },
        })
      })
    }];
    this.subject = new BehaviorSubject(this.layers.slice());
  }

  addLayer(layer: Layer): void {
    this.layers.splice(this.layers.length - 1, 0, layer);
    this.subject.next(this.layers.slice());
  }

  removeLayer(layer: Layer): void {
    const index = this.layers.findIndex(itLayer => layer === itLayer);
    if (index !== -1) {
      this.layers.splice(index, 1);
      this.subject.next(this.layers.slice());
    }
  }

  moveLayerUp(layer: Layer): void {
    const index = this.layers.findIndex(itLayer => layer === itLayer);
    if (index !== -1 && index !== this.layers.length - 1) {
      this.layers.splice(index, 1);
      this.layers.splice(index + 1, 0, layer);
      this.subject.next(this.layers.slice());
    }
  }

  moveLayerDown(layer: Layer): void {
    const index = this.layers.findIndex(itLayer => layer === itLayer);
    if (index !== -1 && index !== 0) {
      this.layers.splice(index, 1);
      this.layers.splice(index - 1, 0, layer);
      this.subject.next(this.layers.slice());
    }
  }

  getLayers(): Observable<Layer[]> {
    return this.subject.asObservable();
  }
}
