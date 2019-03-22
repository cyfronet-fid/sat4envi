import {Component, Inject, Input, OnDestroy, OnInit} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import {Tile, Image, Layer} from 'ol/layer';
import {TileWMS, ImageWMS, OSM} from 'ol/source';
import {UIOverlay} from '../state/overlay/overlay.model';
import proj4 from 'proj4';
import {IConstants, S4E_CONSTANTS} from '../../../app.constants';
import {Product} from '../state/product/product.model';
import {BehaviorSubject, Subject} from 'rxjs';

@Component({
  selector: 's4e-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit, OnDestroy {
  @Input() public overlays: UIOverlay[] = [];
  private baseLayer: Layer;
  private map: Map;

  private activeProduct$ = new BehaviorSubject<Product>(null);

  @Input() public set activeProduct(gr: Product|null) {
    if (gr === null) { return; }
    this.activeProduct$.next(gr);
  }

  constructor(@Inject(S4E_CONSTANTS) private CONSTANTS: IConstants) {}

  ngOnInit(): void {
    const centerOfPolandWebMercator = proj4(this.CONSTANTS.projection.toProjection, this.CONSTANTS.projection.coordinates);
    this.map = new Map({
      target: 'map',
      layers: [],
      view: new View({
        center: centerOfPolandWebMercator,
        zoom: 6,
      }),
    });

    this.baseLayer = new Tile({
      source: new OSM(),
    });

    this.map.getLayers().push(this.baseLayer);
    for (const overlay of this.overlays) {
      this.map.getLayers().push(overlay.olLayer);
    }

    this.activeProduct$.subscribe(gr => this.updateLayers(gr));
  }

  ngOnDestroy(): void {
    this.activeProduct$.complete();
  }

  private updateLayers(product: Product) {
    const mapLayers = this.map.getLayers();
    mapLayers.clear();
    mapLayers.push(this.baseLayer);

    mapLayers.push(new Image({
      source: new ImageWMS({
        url: this.CONSTANTS.geoserverUrl,
        params: {'LAYERS': product.layerName},
      }),
    }));

    for (const overlay of this.overlays) {
      mapLayers.push(overlay.olLayer);
    }
  }
}
