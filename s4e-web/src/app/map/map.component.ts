import {Component, OnInit} from '@angular/core';
import {Observable, of} from 'rxjs';
import Map from 'ol/Map';
import View from 'ol/View';
import { Tile, Image, Layer } from 'ol/layer';
import { TileWMS, ImageWMS, OSM } from 'ol/source';
import {map} from 'rxjs/operators';
import {format} from 'date-fns';
import proj4 from 'proj4';

import {geoserverUrl} from '../constants';
import {Granule} from '../products/granule.model';
import {ProductService} from '../products/product.service';
import {Product} from '../products/product.model';
import {GranuleView} from './granule-view.model';
import {Overlay} from './overlay.model';

interface Day {
  label: string;
  granules: Granule[];
}

@Component({
  selector: 's4e-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss'],
})
export class MapComponent implements OnInit {
  products: Observable<Product[]>;
  days: Observable<Day[]>;

  overlays: Overlay[];
  baseLayer: Layer;
  granuleViews: GranuleView[];
  activeGranuleView: GranuleView | undefined;

  private map: Map;

  constructor(private productViewService: ProductService) { }

  ngOnInit(): void {
    const centerOfPolandWebMercator = proj4('EPSG:3857', [19, 52]);
    this.map = new Map({
      target: 'map',
      layers: [],
      view: new View({
        center: centerOfPolandWebMercator,
        zoom: 6,
      }),
    });
    this.products = this.productViewService.getProducts();

    this.overlays = [{
      type: 'regions',
      olLayer: new Image({
        source: new ImageWMS({
          url: geoserverUrl,
          params: { 'LAYERS': 'test:wojew%C3%B3dztwa' },
        }),
      }),
    }];
    this.baseLayer = new Tile({
      source: new OSM(),
    });
    this.granuleViews = [];
    this.updateLayers();
  }

  selectProduct(product: Product): void {
    const existingGranuleView = this.granuleViews.find(gv => gv.product === product);
    if (existingGranuleView === undefined) {
      const granuleView = {
        product: product,
        granule: undefined
      };
      this.granuleViews.push(granuleView);
      this.setActiveGranuleView(granuleView);
    } else {
      this.setActiveGranuleView(existingGranuleView);
    }
  }

  updateViewedGranule(granule: Granule): void {
    this.activeGranuleView.granule = granule;
    this.updateLayers();
  }

  setActiveGranuleView(granuleView: GranuleView | undefined): void {
    if (granuleView === undefined) {
      this.days = of([]);
      this.activeGranuleView = undefined;
      this.updateLayers();
    } else if (this.activeGranuleView !== granuleView) {
      this.days = this.productViewService.getGranules(granuleView.product.id).pipe(
        map(granules => {
          if (granuleView.granule === undefined) {
            granuleView.granule = granules[granules.length - 1];
            this.updateLayers();
          }
          return granules;
        }),
        map(this.granulesToDays));
      this.activeGranuleView = granuleView;
      this.updateLayers();
    }
  }

  removeGranuleView(productId: number): void {
    const index = this.granuleViews.findIndex(gv => gv.granule.productId === productId);
    if (index !== -1) {
      this.granuleViews.splice(index, 1);
    }
    if (this.activeGranuleView.granule.productId === productId) {
      if (this.granuleViews.length > 0) {
        this.setActiveGranuleView(this.granuleViews[0]);
      } else {
        this.setActiveGranuleView(undefined);
      }
    }
  }

  private updateLayers() {
    const mapLayers = this.map.getLayers();
    mapLayers.clear();
    mapLayers.push(this.baseLayer);
    if (this.activeGranuleView !== undefined && this.activeGranuleView.granule !== undefined) {
      mapLayers.push(new Image({
        source: new ImageWMS({
          url: geoserverUrl,
          params: {'LAYERS': this.activeGranuleView.granule.layerName},
        }),
      }));
    }
    for (const overlay of this.overlays) {
      mapLayers.push(overlay.olLayer);
    }
  }

  private granulesToDays(granules: Granule[]): Day[] {
    const days: Day[] = [];
    let currDay: Day;
    for (const granule of granules) {
      const day = format(granule.timestampDate, 'yyyy-MM-dd');
      if (currDay === undefined || currDay.label !== day) {
        currDay = {
          label: day,
          granules: []
        };
        days.push(currDay);
      }
      currDay.granules.push(granule);
    }
    return days;
  }
}
