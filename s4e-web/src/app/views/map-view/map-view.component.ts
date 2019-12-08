import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Observable} from 'rxjs';
import {UIOverlay} from './state/overlay/overlay.model';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {OverlayQuery} from './state/overlay/overlay.query';
import {OverlayService} from './state/overlay/overlay.service';
import {S4eConfig} from '../../utils/initializer/config.service';
import {MapState, ViewPosition, ZOOM_LEVELS} from './state/map/map.model';
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
import {ActivatedRoute, Router} from '@angular/router';
import proj4 from 'proj4';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {IUILayer} from './state/common.model';
import {switchMap} from 'rxjs/operators';
import {ProfileQuery} from '../../state/profile/profile.query';

@Component({
  selector: 's4e-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.scss'],
})
export class MapViewComponent implements OnInit, OnDestroy {
  productsTypeList$: Observable<IUILayer[]>;
  overlays$: Observable<UIOverlay[]>;

  public loading$: Observable<boolean>;
  public activeScene$: Observable<Scene>;
  public activeProducts$: Observable<Product | null>;
  public scenes$: Observable<Scene[]>;
  public scenesAreLoading$: Observable<boolean>;
  public legend$: Observable<Legend>;
  public legendState$: Observable<LegendState>;
  public activeOverlays$: Observable<UIOverlay[]>;
  public userLoggedIn$: Observable<boolean>;
  public placeSearchResults$: Observable<SearchResult[]>;
  public placeSearchLoading$: Observable<boolean>;
  public placeSearchResultsOpen$: Observable<boolean>;
  public activeView$: Observable<ViewPosition>;
  public currentTimelineDate$: Observable<string>;
  public availableDates$: Observable<string[]>;
  public showZKOptions$: Observable<boolean>;
  public selectedLocation$: Observable<SearchResult | null>;
  public userIsZK$: Observable<boolean>;
  @ViewChild('map', {read: MapComponent}) mapComponent: MapComponent;

  constructor(public mapService: MapService,
              private router: Router,
              private route: ActivatedRoute,
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
              private profileQuery: ProfileQuery,
              private CONFIG: S4eConfig) {
  }

  ngOnInit(): void {
    this.userIsZK$ = this.profileQuery.selectMemberZK();
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
    this.placeSearchLoading$ = this.searchResultsQuery.selectLoading();
    this.placeSearchResults$ = this.searchResultsQuery.selectAll();
    this.placeSearchResultsOpen$ = this.searchResultsQuery.selectIsOpen();
    this.userLoggedIn$ = this.sessionQuery.isLoggedIn$();
    this.availableDates$ = this.productQuery.selectAvailableDates();
    this.showZKOptions$ = this.mapQuery.select('zkOptionsOpened');
    this.productService.get();
    this.overlayService.get();
    this.activeView$ = this.mapQuery.select('view');

    this.mapService.setView({
      centerCoordinates: proj4(this.CONFIG.projection.toProjection, this.CONFIG.projection.coordinates),
      zoomLevel: 6
    });

    this.mapService.connectRouterToStore(this.route).pipe(untilDestroyed(this), switchMap(() => this.mapService.connectStoreToRouter())).subscribe();

    this.productService.get();
    this.overlayService.get();
  }

  selectProduct(productId: number | null) {
    this.productService.toggleActive(productId);
  }

  selectScene(sceneId: number) {
    console.log('selectScene', sceneId);
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

  ngOnDestroy(): void {
  }

  isLinkActive(url: string): boolean {
    return this.route.snapshot.children[0].url[0].path === url;
  }
}
