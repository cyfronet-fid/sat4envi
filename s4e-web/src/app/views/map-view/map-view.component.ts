import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {combineLatest, forkJoin, Observable} from 'rxjs';
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
import {SAVE_CONFIG_MODAL_ID, SaveConfigModal} from './zk/save-config-modal/save-config-modal.model';
import {LIST_CONFIGS_MODAL_ID, ListConfigsModal} from './zk/list-configs-modal/list-configs-modal.model';
import {ViewConfigurationQuery} from './state/view-configuration/view-configuration.query';
import {REPORT_MODAL_ID, ReportModal} from './zk/report-modal/report-modal.model';
import {ActivatedRoute, Router} from '@angular/router';
import proj4 from 'proj4';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {map, switchMap, take} from 'rxjs/operators';
import {ProfileQuery} from '../../state/profile/profile.query';
import {ConfigurationModal, SHARE_CONFIGURATION_MODAL_ID} from './zk/configuration/state/configuration.model';
import {resizeImage} from '../../utils/miscellaneous/miscellaneous';

@Component({
  selector: 's4e-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.scss'],
})
export class MapViewComponent implements OnInit, OnDestroy {
  overlays$: Observable<UIOverlay[]>;

  public loading$: Observable<boolean>;
  public activeScene$: Observable<Scene>;
  public activeProducts$: Observable<Product | null>;
  public scenes$: Observable<Scene[]>;
  public scenesAreLoading$: Observable<boolean>;
  public legend$: Observable<Legend>;
  public legendState$: Observable<LegendState>;
  public userLoggedIn$: Observable<boolean>;
  public placeSearchResults$: Observable<SearchResult[]>;
  public placeSearchLoading$: Observable<boolean>;
  public placeSearchResultsOpen$: Observable<boolean>;
  public activeView$: Observable<ViewPosition>;
  public currentTimelineDate$: Observable<string>;
  public availableDates$: Observable<string[]>;
  public showZKOptions$: Observable<boolean>;
  public showLoginOptions$: Observable<boolean>;
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
              private viewConfigurationQuery: ViewConfigurationQuery,
              private profileQuery: ProfileQuery,
              private CONFIG: S4eConfig) {
  }

  ngOnInit(): void {
    this.userIsZK$ = this.profileQuery.selectMemberZK();
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
    this.showLoginOptions$ = this.mapQuery.select('loginOptionsOpened');
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

  toggleLegend() {
    this.legendService.toggleLegend();
  }

  setDate($event: string) {
    this.sceneService.get(this.productQuery.getActive(), $event);
    this.productService.setSelectedDate($event);
  }

  loadAvailableDates($event: string) {
    this.productService.fetchAvailableDays($event);
  }

  toggleZKOptions(show: boolean = true) {
    this.mapService.toggleZKOptions(show);
  }

  toggleLoginOptions(show: boolean = true) {
    this.mapService.toggleLoginOptions(show);
  }

  downloadMapImage() {
    this.mapComponent.downloadMap();
    this.toggleZKOptions(false);
  }

  openReportModal() {
    forkJoin([
      this.mapComponent.getMapData(),
      this.productQuery.selectActive().pipe(map(p => p == null ? null : p.name), take(1)),
      this.sceneQuery.selectActive().pipe(map(s => s == null ? null : s.timestamp), take(1))
    ])
      .subscribe(([mapData, productName, sceneDate]) => this.modalService.show<ReportModal>({
        id: REPORT_MODAL_ID, size: 'lg',
        mapHeight: mapData.height,
        mapWidth: mapData.width,
        mapImage: mapData.image,
        productName: productName,
        sceneDate: sceneDate
      }));
    this.toggleZKOptions(false);
  }

  openSaveViewModal() {
    this.mapComponent.getMapData().pipe(
      switchMap(data => resizeImage(data.image, 170, 105))
    )
      .subscribe(mapData => this.modalService.show<SaveConfigModal>(
        {
          id: SAVE_CONFIG_MODAL_ID,
          size: 'lg',
          viewConfiguration: {
            ...this.viewConfigurationQuery.getCurrent(),
            thumbnail: mapData
          }
        }
      ));
    this.toggleZKOptions(false);
  }

  openShareViewModal() {
    combineLatest([
      this.mapComponent.getMapData().pipe(switchMap(data => resizeImage(data.image, 400, 247))),
      this.mapQuery.selectQueryParamsFromStore().pipe(take(1), map(query => this.router.serializeUrl(this.router.createUrlTree([], {queryParams: query}))))
    ])
      .subscribe(([mapData, path]) => this.modalService.show<ConfigurationModal>({
        id: SHARE_CONFIGURATION_MODAL_ID, size: 'lg',
        mapImage: mapData,
        configurationUrl: path
      }));
    this.toggleZKOptions(false);
  }

  viewChanged($event: ViewPosition) {
    this.mapService.setView($event);
  }

  openListViewModal() {
    this.modalService.show<ListConfigsModal>({
      id: LIST_CONFIGS_MODAL_ID, size: 'lg'
    });
    this.toggleZKOptions(false);
  }

  ngOnDestroy(): void {
  }

  isLinkActive(url: string): boolean {
    return this.route.snapshot.children[0].url[0].path === url;
  }
}
