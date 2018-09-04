import {Component} from '@angular/core';
import { Tile, Image, Layer as olLayer } from 'ol/layer';

import {Product} from '../products/product.model';

interface Layer {
  product: Product;
  olLayer: olLayer;
}

@Component({
  selector: 's4e-layers',
  templateUrl: './layers.component.html',
  styleUrls: ['./layers.component.scss']
})
export class LayersComponent {
  layers: Layer[];

  constructor() {
    this.layers = [];
  }
}
