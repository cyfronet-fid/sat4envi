import {RemoteConfiguration} from 'src/app/utils/initializer/config.service';
import {environment} from '../../../../environments/environment';
import {NotificationService} from 'notifications';
import {TileLoader} from '../state/utils/layers-loader.util';
import {SessionQuery} from '../../../state/session/session.query';
import {Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import {Layer, Tile} from 'ol/layer';
import {OSM, TileWMS} from 'ol/source';
import {UIOverlay} from '../state/overlay/overlay.model';
import proj4 from 'proj4';
import {Scene} from '../state/scene/scene.model';
import {BehaviorSubject, combineLatest, Observable, of, ReplaySubject} from 'rxjs';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {distinctUntilChanged, filter, map} from 'rxjs/operators';
import {MapData, ViewPosition} from '../state/map/map.model';
import moment from 'moment';
import {NgxUiLoaderService} from 'ngx-ui-loader';
import VectorSource from 'ol/source/Vector';
import VectorLayer from 'ol/layer/Vector';
import {Draw, Modify, Snap} from 'ol/interaction';
import GeometryType from 'ol/geom/GeometryType';
import {WKT} from 'ol/format';
import {SentinelSearchService} from '../state/sentinel-search/sentinel-search.service';
import {SentinelSearchQuery} from '../state/sentinel-search/sentinel-search.query';
import BaseLayer from 'ol/layer/Base';
import {getPointResolution} from 'ol/proj';

@Component({
  selector: 's4e-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit, OnDestroy {
  static readonly DEFAULT_ZOOM_LEVEL = 10;
  @Output() viewChanged = new EventEmitter<ViewPosition>();
  @ViewChild('linkDownload', {read: ElementRef}) linkDownload: ElementRef;

  activeView$ = new BehaviorSubject<ViewPosition>({
    centerCoordinates: environment.projection.coordinates,
    zoomLevel: MapComponent.DEFAULT_ZOOM_LEVEL
  });

  @Input()
  set isSentinelSearch(isSentinelSearch: boolean) {
    this._isSentinelSearch = isSentinelSearch;

    if (!this.map) {
      return;
    }

    if (!isSentinelSearch) {
      this._clearPolygonDrawing();
    }
  }

  private _isSentinelSearch = false;
  private _removePolygon = false;

  private overlays$: BehaviorSubject<UIOverlay[]> = new BehaviorSubject([]);
  private baseLayer: Layer;
  private map: Map;
  private activeScene$ = new ReplaySubject<Scene | null>(1);
  private _sentinelPolygonVectorLayer: BaseLayer;

  private _polygonDrawing = {
    layer: new VectorLayer({
      source: new VectorSource()
    }),
    drawing: null,
    polygon: null
  };

  constructor(
    private _remoteConfiguration: RemoteConfiguration,
    private _loaderService: NgxUiLoaderService,
    private _notificationService: NotificationService,
    private _sessionQuery: SessionQuery,
    private _sentinelSearchQuery: SentinelSearchQuery,
    private _sentinelSearchService: SentinelSearchService
  ) {}

  private _overlays: UIOverlay[] = [];

  get overlays(): UIOverlay[] {
    return this._overlays;
  }

  @Input() set overlays(value: UIOverlay[]) {
    this._overlays = value;
    this.overlays$.next(value);
  }

  @Input()
  public set activeScene(gr: Scene | null | undefined) {
    this.activeScene$.next(gr);
  }

  @Input()
  public set activeView(view: ViewPosition | null) {
    this.activeView$.next(view);
  }

  ngOnInit(): void {
    const centerOfPolandWebMercator = proj4(
      environment.projection.toProjection,
      this.activeView$.getValue().centerCoordinates
    );
    this.map = new Map({
      target: 'map',
      layers: [],
      view: new View({
        center: centerOfPolandWebMercator,
        zoom: this.activeView$.getValue().zoomLevel,
        maxZoom: environment.maxZoom
      }),
    });
    this.map.on(
      'rendercomplete',
      () => setTimeout(
        () => this._loaderService.stopBackground(),
        500
      )
    );

    const source = new OSM({url: '/osm/{z}/{x}/{y}.png', crossOrigin: 'Anonymous'});
    this.baseLayer = new Tile({source});

    this.map.getLayers().push(this.baseLayer);
    for (const overlay of this.overlays) {
      this.map.getLayers().push(overlay.olLayer);
    }

    combineLatest([
      this.activeScene$.pipe(distinctUntilChanged()),
      this.overlays$.pipe(distinctUntilChanged())
    ])
      .pipe(untilDestroyed(this))
      .subscribe(([gr, overlays]) => {
        this.overlays = overlays;
        this.updateLayers(gr);
      });

    this._sentinelSearchQuery.selectHovered()
      .pipe(
        untilDestroyed(this),
        filter(hovered => !!hovered || !!this._sentinelPolygonVectorLayer),
        filter(hovered => {
          if (!hovered) {
            this.map.removeLayer(this._sentinelPolygonVectorLayer);
            this._sentinelPolygonVectorLayer = null;
          }

          return !!hovered;
        }),
        map(hovered => new WKT().readFeature(
          hovered.footprint,
          {
            dataProjection: 'EPSG:4326',
            featureProjection: 'EPSG:3857',
          }
        )),
        map(polygonFeature => {
          this._sentinelPolygonVectorLayer = new VectorLayer({
            source: new VectorSource({
              features: [polygonFeature],
            }),
          });
          return this._sentinelPolygonVectorLayer;
        })
      )
      .subscribe(polygonVector => this.map.addLayer(polygonVector));

    this.map.on('moveend', this.onMoveEnd);
    this.map.on('click', () => {
      if (!this._isSentinelSearch) {
        return;
      }

      const hasDrawingLayer = this.map.getLayers().getArray()
        .includes(this._polygonDrawing.layer);
      if (!hasDrawingLayer) {
        this._addPolygonDrawing();
        return;
      }

      if (this._removePolygon) {
        this._clearPolygonDrawing();
        this._removePolygon = false;
        return;
      }

      const isDrawing = this.map.getInteractions().getArray()
        .includes(this._polygonDrawing.drawing);
      if (isDrawing) {
        return;
      }

      const hasPolygon = this.map.getLayers().getArray()
        .includes(this._polygonDrawing.polygon);
      if (!this._removePolygon && hasPolygon) {
        this._removePolygon = true;
        return;
      }
    });
    this.activeView$.pipe(untilDestroyed(this)).subscribe(view => this.setView(view));
  }

  ngOnDestroy(): void {
    this.activeScene$.complete();
    this.map.un('moveend', this.onMoveEnd);
  }

  onMoveEnd = (event: any) => {
    const view = event.map.getView();
    this.viewChanged.emit({
      centerCoordinates: view.getCenter(),
      zoomLevel: view.getZoom()
    });
  };

  public getMapData(): Observable<MapData | null> {
    if (!this.map) {
      return of(null);
    }


    const mapCanvas: HTMLCanvasElement = this.map.getViewport().firstChild as HTMLCanvasElement;

    const pointResolution = getPointResolution(
      this.map.getView().getProjection(),
      this.map.getView().getResolution(),
      this.map.getView().getCenter()
    );

    const r = new ReplaySubject<MapData>(1);

    // :IMPORTANT this will work only on new browsers!
    this.map.once('rendercomplete', () => {
      const data = mapCanvas.toDataURL('image/png');
      r.next({image: data, width: mapCanvas.width, height: mapCanvas.height, pointResolution});
      r.complete();
    });
    this.map.renderSync();

    return r;
  }

  public downloadMap() {
    this.getMapData()
      .subscribe(mapData => {
        this.linkDownload.nativeElement.setAttribute('download', `SNAPSHOT.${new Date().toISOString()}.png`);
        this.linkDownload.nativeElement.href = mapData.image;
        this.linkDownload.nativeElement.click();
      });
  }

  public updateSize() {
    this.map.updateSize();
  }

  private setView(view: ViewPosition): void {
    if (view.centerCoordinates != null) {
      this.map.getView().setCenter(view.centerCoordinates);
    }
    if (view.zoomLevel != null) {
      this.map.getView().setZoom(view.zoomLevel);
    }
  }

  private updateLayers(scene: Scene | null) {
    const mapLayers = this.map.getLayers();
    mapLayers.clear();
    mapLayers.push(this.baseLayer);

    if (scene != null) {
      const sceneTiles = new Tile({source: this._getTileWmsFrom(scene)});
      mapLayers.push(sceneTiles);
    }

    const activeLayers = this.overlays
      .filter(overlay => overlay.active)
      .map(overlay => overlay.olLayer);
    mapLayers.extend(activeLayers);
  }

  private _getTileWmsFrom(scene: Scene) {
    const utcTime = moment(scene.timestamp).utc();

    // IMPORTANT!!!
    // Due to change in geo-server we need floor timestamp to seconds
    // If not, it will not respond correctly
    const isoTimeWithoutMs = utcTime.format('YYYY-MM-DD[T]HH:mm:ss[Z]');
    const source = new TileWMS({
      crossOrigin: 'Anonymous',
      url: this._remoteConfiguration.get().geoserverUrl,
      serverType: 'geoserver',
      params: {
        'LAYERS': this._remoteConfiguration.get().geoserverWorkspace + ':' + scene.layerName,
        'TIME': isoTimeWithoutMs,
        'TILED': true,
      },
    });

    new TileLoader(source).start$.then(() => this._loaderService.startBackground());

    return source;
  }

  private _addPolygonDrawing() {
    this.map.getLayers().push(this._polygonDrawing.layer);
    this._polygonDrawing.drawing = new Draw({
      source: this._polygonDrawing.layer.getSource(),
      type: GeometryType.POLYGON
    });
    this.map.addInteraction(this._polygonDrawing.drawing);
    this._polygonDrawing.drawing
      .on('drawstart', () => {
        this._polygonDrawing.layer.getSource()
          .forEachFeature(feature => this._polygonDrawing.layer.getSource().removeFeature(feature));
        if (!!this._polygonDrawing.polygon) {
          this.map.removeLayer(this._polygonDrawing.polygon);
        }
      });
    this._polygonDrawing.drawing
      .on('drawend', (event) => {
        const polygonGeometry = event.feature.getGeometry();
        const polygonWkt = new WKT().writeGeometry(polygonGeometry, {
          dataProjection: 'EPSG:4326',
          featureProjection: 'EPSG:3857'
        });
        this._sentinelSearchService.setFootprint(polygonWkt);
        this._polygonDrawing.polygon = new VectorLayer({
          source: new VectorSource({
            features: [new WKT().readFeature(
              polygonWkt,
              {
                dataProjection: 'EPSG:4326',
                featureProjection: 'EPSG:3857'
              }
            )],
          }),
        });
        this.map.addLayer(this._polygonDrawing.polygon);
        this.map.removeInteraction(this._polygonDrawing.drawing)
      });
  }

  private _clearPolygonDrawing() {
    this._sentinelSearchService.setFootprint(null);

    if (!!this._polygonDrawing.layer) {
      this.map.removeLayer(this._polygonDrawing.layer);
      this._polygonDrawing.layer.getSource()
        .forEachFeature(feature => this._polygonDrawing.layer.getSource().removeFeature(feature));
    }

    if (!!this._polygonDrawing.polygon) {
      this.map.removeLayer(this._polygonDrawing.polygon);
    }

    if (!!this._polygonDrawing.drawing) {
      this.map.removeInteraction(this._polygonDrawing.drawing);
    }
  }
}
