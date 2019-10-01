import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import {Image, Layer, Tile} from 'ol/layer';
import {ImageWMS, OSM} from 'ol/source';
import {UIOverlay} from '../state/overlay/overlay.model';
import proj4 from 'proj4';
import {Product} from '../state/product/product.model';
import {ReplaySubject} from 'rxjs';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {S4eConfig} from '../../../utils/initializer/config.service';

@Component({
  selector: 's4e-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit, OnDestroy {
  @Input() public overlays: UIOverlay[] = [];
  private baseLayer: Layer;
  private map: Map;

  private activeProduct$ = new ReplaySubject<Product | null>(1);

  constructor(private CONFIG: S4eConfig) {
  }

  @Input()
  public set activeProduct(gr: Product | null | undefined) {
    this.activeProduct$.next(gr);
  }

  ngOnInit(): void {
    const centerOfPolandWebMercator = proj4(this.CONFIG.projection.toProjection, this.CONFIG.projection.coordinates);
    this.map = new Map({
      target: 'map',
      layers: [],
      view: new View({
        center: centerOfPolandWebMercator,
        zoom: 6,
        maxZoom: 12
      }),
    });

    this.baseLayer = new Tile({
      source: new OSM({url: '/osm/{z}/{x}/{y}.png'}),
    });

    this.map.getLayers().push(this.baseLayer);
    for (const overlay of this.overlays) {
      this.map.getLayers().push(overlay.olLayer);
    }

    this.activeProduct$.pipe(untilDestroyed(this)).subscribe(gr => this.updateLayers(gr));
  }

  ngOnDestroy(): void {
    this.activeProduct$.complete();
  }

  private updateLayers(product: Product | null) {
    console.log('updateLayers', product);
    const mapLayers = this.map.getLayers();
    mapLayers.clear();
    mapLayers.push(this.baseLayer);

    if (product != null) {
      mapLayers.push(new Image({
        source: new ImageWMS({
          url: this.CONFIG.geoserverUrl,
          params: {'LAYERS': this.CONFIG.geoserverWorkspace + ':' + product.layerName},
        }),
      }));
    }

    for (const overlay of this.overlays) {
      mapLayers.push(overlay.olLayer);
    }
  }
}
