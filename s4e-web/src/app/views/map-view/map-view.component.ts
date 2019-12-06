import {Component, OnInit, ViewChild} from '@angular/core';
import {Observable} from 'rxjs';
import {UIOverlay} from './state/overlay/overlay.model';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {OverlayQuery} from './state/overlay/overlay.query';
import {OverlayService} from './state/overlay/overlay.service';
import {S4eConfig} from '../../utils/initializer/config.service';
import {ViewPosition} from './state/map/map.model';
import {Legend, LegendState} from './state/legend/legend.model';
import {LegendQuery} from './state/legend/legend.query';
import {LegendService} from './state/legend/legend.service';
import {SearchResult} from './state/search-results/search-result.model';
import {SearchResultsQuery} from './state/search-results/search-results.query';
import {SessionService} from '../../state/session/session.service';
import {SessionQuery} from '../../state/session/session.query';
import {Scene} from './state/scene/scene.model';
import {Product} from './state/product/product.model';
import {SceneService} from './state/scene/scene.service';
import {ProductService} from './state/product/product.service';
import {ProductQuery} from './state/product/product.query';
import {SceneQuery} from './state/scene/scene.query.service';
import {MapComponent} from './map/map.component';
import {ModalService} from '../../modal/state/modal.service';
import {REPORT_MODAL_ID, ReportModal} from './report-modal/report-modal.model';

@Component({
  selector: 's4e-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.scss'],
})
export class MapViewComponent implements OnInit {
  public overlays$: Observable<UIOverlay[]>;
  public loading$: Observable<boolean>;
  public activeScene$: Observable<Scene>;
  public activeProducts$: Observable<Product | null>;
  public scenes$: Observable<Scene[]>;
  public scenesAreLoading$: Observable<boolean>;
  public legend$: Observable<Legend>;
  public legendState$: Observable<LegendState>;
  public activeOverlays$: Observable<UIOverlay[]>;
  public userLoggedIn$: Observable<boolean>;
  public currentTimelineDate$: Observable<string>;
  public availableDates$: Observable<string[]>;
  public showZKOptions$: Observable<boolean>;
  public selectedLocation$: Observable<SearchResult | null>;
  @ViewChild('map', {read: MapComponent}) mapComponent: MapComponent;

  constructor(private mapService: MapService,
              private mapQuery: MapQuery,
              private overlayQuery: OverlayQuery,
              private overlayService: OverlayService,
              private sceneService: SceneService,
              private productService: ProductService,
              private productQuery: ProductQuery,
              private sceneQuery: SceneQuery,
              private sessionQuery: SessionQuery,
              private sessionService: SessionService,
              private legendQuery: LegendQuery,
              private legendService: LegendService,
              private searchResultsQuery: SearchResultsQuery,
              private modalService: ModalService,
              private CONFIG: S4eConfig) {
  }

  ngOnInit(): void {
    this.selectedLocation$ = this.searchResultsQuery.selectLocation();
    this.loading$ = this.mapQuery.selectLoading();
    this.currentTimelineDate$ = this.productQuery.selectSelectedDate();
    this.activeScene$ = this.sceneQuery.selectActive();
    this.scenes$ = this.sceneQuery.selectAll();
    this.scenesAreLoading$ = this.sceneQuery.selectLoading();
    this.overlays$ = this.overlayQuery.selectAllAsUIOverlays();
    this.activeProducts$ = this.productQuery.selectActive();
    this.legend$ = this.legendQuery.selectLegend();
    this.legendState$ = this.legendQuery.select();
    this.userLoggedIn$ = this.sessionQuery.isLoggedIn$();
    this.availableDates$ = this.productQuery.selectAvailableDates();
    this.showZKOptions$ = this.mapQuery.select('zkOptionsOpened');
    this.productService.get();
    this.overlayService.get();
  }


  selectScene(sceneId: number) {
    this.sceneService.setActive(sceneId);
  }


  logout() {
    this.sessionService.logout();
  }

  toggleLegend() {
    this.legendService.toggleLegend();
  }

  setDate($event: string) {
    this.sceneService.get(this.productQuery.getActiveId(), $event);
    this.productService.setSelectedDate($event);
  }

  loadAvailableDates($event: string) {
    this.productService.fetchAvailableDays($event);
  }

  toggleZKOptions(show: boolean = true) {
    this.mapService.toggleZKOptions(show);
  }


  downloadMapImage() {
    this.mapComponent.downloadMap();
    this.toggleZKOptions(false);
  }

  openReportModal() {
    this.mapComponent.getMapData()
      .subscribe(mapData => this.modalService.show<ReportModal>({
        id: REPORT_MODAL_ID, size: 'lg',
        mapHeight: mapData.height,
        mapWidth: mapData.width,
        mapImage: mapData.image,
      }));
    this.toggleZKOptions(false);
  }

  openSaveViewModal() {
    this.modalService.alert('Not Implemented', 'This feature is not yet implemented');
    this.toggleZKOptions(false);
  }

  openShareViewModal() {
    this.modalService.alert('Not Implemented', 'This feature is not yet implemented');
    this.toggleZKOptions(false);
  }

 viewChanged($event: ViewPosition) {
    this.mapService.setView($event);
  }
}
