import { NotificationService } from './../../../../../projects/notifications/src/lib/state/notification.service';
import { ImageWmsLoader, IMAGE_WMS_LAYER } from './../state/utils/layers-loader.util';
import { SessionQuery } from './../../../state/session/session.query';
import {Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import {Image, Layer, Tile} from 'ol/layer';
import {ImageWMS, OSM} from 'ol/source';
import {UIOverlay} from '../state/overlay/overlay.model';
import proj4 from 'proj4';
import {Scene} from '../state/scene/scene.model';
import {BehaviorSubject, combineLatest, Observable, ReplaySubject} from 'rxjs';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {S4eConfig} from '../../../utils/initializer/config.service';
import {distinctUntilChanged} from 'rxjs/operators';
import {MapData, ViewPosition} from '../state/map/map.model';
import moment from 'moment';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { getImageXhr, ImageBase64 } from '../../settings/manage-institutions/institution-form/files.utils';
import ImageWrapper from 'ol/Image';

@Component({
  selector: 's4e-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit, OnDestroy {
  static readonly DEFAULT_ZOOM_LEVEL = 10;

  get overlays(): UIOverlay[] {
    return this._overlays;
  }

  @Input() set overlays(value: UIOverlay[]) {
    this._overlays = value;
    this.overlays$.next(value);
  }

  private _overlays: UIOverlay[] = [];

  private overlays$: BehaviorSubject<UIOverlay[]> = new BehaviorSubject([]);
  @Output() viewChanged = new EventEmitter<ViewPosition>();
  private baseLayer: Layer;
  private map: Map;
  private activeScene$ = new ReplaySubject<Scene | null>(1);
  @ViewChild('linkDownload', {read: ElementRef}) linkDownload: ElementRef;
  @Input() isWorking: boolean = false;
  @Output() working = new EventEmitter<boolean>();
  activeView$ = new BehaviorSubject<ViewPosition>({
    centerCoordinates: this.CONFIG.projection.coordinates,
    zoomLevel: MapComponent.DEFAULT_ZOOM_LEVEL
  });

  constructor(
    private CONFIG: S4eConfig,
    private _loaderService: NgxUiLoaderService,
    private _notificationService: NotificationService,
    private _sessionQuery: SessionQuery
  ) {}

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
      this.CONFIG.projection.toProjection,
      this.activeView$.getValue().centerCoordinates
    );
    this.map = new Map({
      target: 'map',
      layers: [],
      view: new View({
        center: centerOfPolandWebMercator,
        zoom: this.activeView$.getValue().zoomLevel,
        maxZoom: this.CONFIG.maxZoom
      }),
    });

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

    this.map.on('moveend', this.onMoveEnd);
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
      const source = new ImageWMS({
        crossOrigin: 'Anonymous',
        url: this.CONFIG.geoserverUrl,
        serverType: 'geoserver',
        params: {
          'LAYERS': this.CONFIG.geoserverWorkspace + ':' + scene.layerName,
          'TIME': moment(scene.timestamp).utc().toISOString(),
        },
      });

      const imageWmsLoader = new ImageWmsLoader(source);
      imageWmsLoader.start$
        .then(
          () => this._loaderService.startBackground(),
          () => this._handleLoadError()
        );
      imageWmsLoader.end$
        .then(
          () => this._loaderService.stopBackground(),
          () => this._handleLoadError()
        );

      const image = new Image({ source });
      mapLayers.push(image);
      source.setImageLoadFunction(this._getImageLoaderWith('Bearer ' + this._sessionQuery.getToken()));
      mapLayers.push(new Image({ source }));
    }

    for (const overlay of this.overlays.filter(ol => ol.active)) {
      mapLayers.push(overlay.olLayer);
    }
  }

  public getMapData(): Observable<MapData> {
    this.working.emit(true);
    const canvas: HTMLCanvasElement = this.map.getViewport().firstChild as HTMLCanvasElement;

    const r = new ReplaySubject<MapData>(1);

    // this will work only on new browsers!
    this.map.once('rendercomplete', () => {
      const data = canvas.toDataURL('image/png');
      r.next({image: data, width: canvas.width, height: canvas.height});
      this.working.emit(false);
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

  protected _handleLoadError() {
    this._loaderService.stopBackground();
    this._notificationService.addGeneral({
      type: 'error',
      content: 'Wczytanie sceny nie powiodło się'
    });
  }
  private _getImageLoaderWith(bearer: string) {
    return function (tile: ImageWrapper, src: string) {
      const xhr = getImageXhr(src, bearer);
      xhr.onload = () => ((tile.getImage() as HTMLImageElement).src = ImageBase64.getFromXhr(xhr));
      xhr.send();

      return xhr;
    };
  }
}
