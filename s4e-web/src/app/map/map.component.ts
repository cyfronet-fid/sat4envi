import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import Map from 'ol/Map';
import View from 'ol/View';
import { Tile, Image, Layer } from 'ol/layer';
import { ImageWMS, OSM } from 'ol/source';

import {geoserverUrl} from '../constants';
import {Granule} from '../products/granule.model';
import {ProductService} from '../products/product.service';
import {Product} from '../products/product.model';
import {GranuleView} from './granule-view.model';
import {Overlay} from './overlay.model';

@Component({
  selector: 's4e-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss'],
})
export class MapComponent implements OnInit {
  products: Observable<Product[]>;
  selectedProduct: Product | undefined;
  granules: Observable<Granule[]>;

  overlays: Overlay[];
  granuleViews: GranuleView[];
  activeGranuleView: GranuleView | undefined;

  private map: Map;

  constructor(private productViewService: ProductService) { }

  ngOnInit(): void {
    this.map = new Map({
      target: 'map',
      layers: [],
      view: new View({
        center: [2158581, 6841419],
        zoom: 6
      })
    });
    this.products = this.productViewService.getProducts();

    this.overlays = [{
      type: 'regions',
      olLayer: new Image({
        source: new ImageWMS({
          url: geoserverUrl,
          params: { 'LAYERS': 'test:wojewodztwa' },
        }),
      }),
    }];
    this.granuleViews = [];
    this.updateLayers();
  }

  selectProduct(product: Product): void {
    this.selectedProduct = product;
    this.granules = this.productViewService.getGranules(product.id);
  }

  addGranuleView(granule: Granule, product: Product): void {
    const existingGranuleView = this.granuleViews.find(gv => gv.granule.productId === granule.productId);
    if (existingGranuleView !== undefined) {
      existingGranuleView.granule = granule;
    } else {
      const granuleView = {
        product: product,
        granule: granule
      };
      this.granuleViews.push(granuleView);
      this.selectGranuleView(granuleView);
    }
  }

  selectGranuleView(granuleView: GranuleView): void {
    this.activeGranuleView = granuleView;
    this.updateLayers();
  }

  removeGranuleView(productId: number): void {
    const index = this.granuleViews.findIndex(gv => gv.granule.productId === productId);
    if (index !== -1) {
      this.granuleViews.splice(index, 1);
    }
    if (this.activeGranuleView.granule.productId === productId) {
      if (this.granuleViews.length > 0) {
        this.selectGranuleView(this.granuleViews[0]);
      } else {
        this.selectGranuleView(undefined);
      }
    }
  }

  private updateLayers() {
    const mapLayers = this.map.getLayers();
    mapLayers.clear();
    mapLayers.push(new Tile({
      source: new OSM(),
    }));
    if (this.activeGranuleView !== undefined) {
      mapLayers.push(new Image({
        source: new ImageWMS({
          url: geoserverUrl,
          params: {'LAYERS': this.activeGranuleView.granule.layerName}
        })
      }));
    }
    for (const overlay of this.overlays) {
      mapLayers.push(overlay.olLayer);
    }
  }
}
