import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import Feature from 'ol/Feature';
import {Image, Layer, Tile, Vector as VectorLayer} from 'ol/layer';
import {ImageWMS, OSM, Vector} from 'ol/source';
import {Icon, Style} from 'ol/style';
import {UIOverlay} from '../state/overlay/overlay.model';
import proj4 from 'proj4';
import {Product} from '../state/product/product.model';
import {Observable, ReplaySubject, Subscription} from 'rxjs';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {S4eConfig} from '../../../utils/initializer/config.service';
import {SearchResult} from '../state/search-results/search-result.model';
import {Point} from 'ol/geom';


@Component({
  selector: 's4e-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit, OnDestroy {
  @Input() public overlays: UIOverlay[] = [];
  function;
  selectedLocationSub: Subscription = null;
  private baseLayer: Layer;
  private map: Map;
  private activeProduct$ = new ReplaySubject<Product | null>(1);
  private markerSource: Vector = new Vector();

  constructor(private CONFIG: S4eConfig) {
  }

  @Input()
  public set activeProduct(gr: Product | null | undefined) {
    this.activeProduct$.next(gr);
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

    this.activeProduct$.pipe(untilDestroyed(this)).subscribe(gr => this.updateLayers(gr));

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
    this.activeProduct$.complete();
    this.selectedLocationSub.unsubscribe();
    this.selectedLocationSub = null;
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
