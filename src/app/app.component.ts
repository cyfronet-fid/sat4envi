import {Component, OnInit} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import { Tile, Image } from 'ol/layer';
import { ImageWMS, OSM } from 'ol/source';

import {ProductViewService} from './product-views/product-view.service';
import {ProductView} from './product-views/product-view.model';
import {Product} from './products/product.model';
import {geoserverUrl} from './constants';

@Component({
  selector: 's4e-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  productViews: Array<ProductView>;
  selectedProductView: ProductView;
  selectedLayer: Product;

  private productViewService: ProductViewService;
  private map: Map;

  constructor(productViewService: ProductViewService) {
    this.productViewService = productViewService;
  }

  ngOnInit(): void {
    this.map = new Map({
      target: 'map',
      layers: [
        new Tile({
          source: new OSM()
        })
      ],
      view: new View({
        center: [2158581, 6841419],
        zoom: 6
      })
    });
    this.productViews = this.productViewService.getViews();
    this.selectProductView(this.productViews[0]);
  }

  selectProductView(productView: ProductView): void {
    this.selectedProductView = productView;
    this.showLayer(this.selectedProductView.layers[0]);
  }

  private showLayer(layer: Product): void {
    this.selectedLayer = layer;
    const layers = this.map.getLayers();
    layers.clear();
    layers.push(new Tile({
      source: new OSM()
    }));
    layers.push(new Image({
      source: new ImageWMS({
        url: geoserverUrl,
        params: { 'LAYERS': `${layer.layerName},test:wojewodztwa` }
      })
    }));
  }
}
