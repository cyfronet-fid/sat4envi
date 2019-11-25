import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import Feature from 'ol/Feature';
import {Image, Layer, Tile, Vector as VectorLayer} from 'ol/layer';
import {ImageWMS, OSM, Vector} from 'ol/source';
import {Icon, Style} from 'ol/style';
import {UIOverlay} from '../state/overlay/overlay.model';
import proj4 from 'proj4';
import {Scene} from '../state/scene/scene.model';
import {BehaviorSubject, combineLatest, Observable, ReplaySubject, Subscription} from 'rxjs';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {S4eConfig} from '../../../utils/initializer/config.service';
import {SearchResult} from '../state/search-results/search-result.model';
import {Point} from 'ol/geom';
import {distinctUntilChanged} from 'rxjs/operators';


@Component({
  selector: 's4e-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit, OnDestroy {
  get overlays(): UIOverlay[] {
    return this._overlays;
  }

  @Input() set overlays(value: UIOverlay[]) {
    console.log(this._overlays);
    this._overlays = value;
    this.overlays$.next(value);
  }
  private _overlays: UIOverlay[] = [];

  private overlays$: BehaviorSubject<UIOverlay[]> = new BehaviorSubject([]);
  function;
  selectedLocationSub: Subscription = null;
  private baseLayer: Layer;
  private map: Map;
  private activeScene$ = new ReplaySubject<Scene | null>(1);
  private markerSource: Vector = new Vector();

  constructor(private CONFIG: S4eConfig) {
  }

  @Input()
  public set activeScene(gr: Scene | null | undefined) {
    this.activeScene$.next(gr);
  }

  @Input()
  public set selectedLocation(place$: Observable<SearchResult | null>) {
    if (this.selectedLocationSub != null) {
      this.selectedLocationSub.unsubscribe();
    }

    this.selectedLocationSub = place$.subscribe(place => {
      this.markerSource.clear();

      if (place == null) {
        return;
      }

      const coord: number[] = [place.longitude, place.latitude];
      this.map.getView().setCenter(proj4(this.CONFIG.projection.toProjection, coord));
      this.addMarker(coord);
    });
  }

  addMarker(coordinates: number[]) {
    const iconFeature = new Feature({
      geometry: new Point(
        proj4(this.CONFIG.projection.toProjection, coordinates)
      )
    });

    this.markerSource.addFeature(iconFeature);
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

    combineLatest([
      this.activeScene$.pipe(distinctUntilChanged()),
      this.overlays$.pipe(distinctUntilChanged())
    ]).pipe(untilDestroyed(this)).subscribe(([gr, overlays]) => {
      this.overlays = overlays;
      this.updateLayers(gr)
    });

    const markerStyle = new Style({
      image: new Icon({
        anchor: [0.5, 220],
        anchorXUnits: 'fraction',
        anchorYUnits: 'pixel',
        opacity: 0.75,
        scale: 0.25,
        src: 'https://cdn2.iconfinder.com/data/icons/ui-26/128/map-pin-location-256.png'
      })
    });
    this.map.getLayers().push(new VectorLayer({
      source: this.markerSource,
      style: markerStyle,
    }));
  }

  ngOnDestroy(): void {
    this.activeScene$.complete();
    this.selectedLocationSub.unsubscribe();
    this.selectedLocationSub = null;
  }

  private updateLayers(product: Scene | null) {
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

    for (const overlay of this.overlays.filter(ol => ol.active)) {
      mapLayers.push(overlay.olLayer);
    }
  }
}
