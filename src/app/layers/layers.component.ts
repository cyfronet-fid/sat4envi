import {Component, OnDestroy, OnInit} from '@angular/core';
import { Tile, Image } from 'ol/layer';
import {Subscription} from 'rxjs';

import {Layer} from './layer.model';
import {LayersService} from './layers.service';


@Component({
  selector: 's4e-layers',
  templateUrl: './layers.component.html',
  styleUrls: ['./layers.component.scss'],
})
export class LayersComponent implements OnInit, OnDestroy {
  layers: Layer[];

  private layersService: LayersService;
  private layersSub: Subscription;

  constructor(layersService: LayersService) {
    this.layersService = layersService;
  }

  ngOnInit(): void {
    this.layersSub = this.layersService.getLayers().subscribe(layers => this.layers = layers.slice().reverse());
  }

  ngOnDestroy(): void {
    this.layersSub.unsubscribe();
  }

  moveDown(layer: Layer): void {
    this.layersService.moveLayerDown(layer);
  }

  moveUp(layer: Layer): void {
    this.layersService.moveLayerUp(layer);
  }

  remove(layer: Layer): void {
    this.layersService.removeLayer(layer);
  }

  setOpacity(opacity: number, layer: Layer): void {
    layer.olLayer.setOpacity(opacity);
  }
}
