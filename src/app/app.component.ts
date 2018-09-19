import {Component, OnDestroy, OnInit} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import { Tile, Image } from 'ol/layer';
import { ImageWMS, OSM } from 'ol/source';

import {ProductViewService} from './product-views/product-view.service';
import {ProductView} from './product-views/product-view.model';
import {Product} from './products/product.model';
import {geoserverUrl} from './constants';
import {LayersService} from './layers/layers.service';
import {Layer} from './layers/layer.model';
import {Subscription} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 's4e-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  productViews: ProductView[];
  selectedProductView: ProductView;

  private map: Map;
  private layers$: Subscription;

  constructor(private productViewService: ProductViewService,
              private layersService: LayersService,
              private translate: TranslateService) {
    this.translate.setDefaultLang('pl');
    this.translate.use('pl');
  }

  ngOnInit(): void {
    this.map = new Map({
      target: 'map',
      layers: [],
      view: new View({
        center: [2158581, 6841419],
        zoom: 6
      })
    });
    this.productViews = this.productViewService.getViews();
    this.selectedProductView = this.productViews[0];
    this.layers$ = this.layersService.getLayers().subscribe(layers => this.updateLayers(layers));
  }

  ngOnDestroy(): void {
    this.layers$.unsubscribe();
  }

  private addLayer(layer: Product): void {
    this.layersService.addLayer({
      product: layer,
      olLayer: new Image({
        source: new ImageWMS({
          url: geoserverUrl,
          params: { 'LAYERS': layer.layerName }
        })
      })
    });
  }

  private updateLayers(layers: Layer[]) {
    const mapLayers = this.map.getLayers();
    mapLayers.clear();
    for (const layer of layers) {
      mapLayers.push(layer.olLayer);
    }
  }
}
