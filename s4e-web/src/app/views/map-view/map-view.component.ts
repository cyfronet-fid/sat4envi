import { JWT_TOKEN_MODAL_ID } from './jwt-token-modal/jwt-token-modal.model';
import { Modal } from './../../modal/state/modal.model';
import { environment } from 'src/environments/environment';
import {Component, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {combineLatest, forkJoin, Observable} from 'rxjs';
import {UIOverlay} from './state/overlay/overlay.model';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {OverlayQuery} from './state/overlay/overlay.query';
import {OverlayService} from './state/overlay/overlay.service';
import {ViewPosition} from './state/map/map.model';
import {Legend, LegendState} from './state/legend/legend.model';
import {LegendQuery} from './state/legend/legend.query';
import {LegendService} from './state/legend/legend.service';
import {LocationSearchResult} from './state/location-search-results/location-search-result.model';
import {SessionService} from '../../state/session/session.service';
import {SessionQuery} from '../../state/session/session.query';
import {Scene, SceneWithUI} from './state/scene/scene.model';
import {Product} from './state/product/product.model';
import {SceneService} from './state/scene/scene.service';
import {ProductService} from './state/product/product.service';
import {ProductQuery} from './state/product/product.query';
import {SceneQuery, TimelineUI} from './state/scene/scene.query';
import {MapComponent} from './map/map.component';
import {ModalService} from '../../modal/state/modal.service';
import {SAVE_CONFIG_MODAL_ID, SaveConfigModal} from './zk/save-config-modal/save-config-modal.model';
import {LIST_CONFIGS_MODAL_ID, ListConfigsModal} from './zk/list-configs-modal/list-configs-modal.model';
import {ViewConfigurationQuery} from './state/view-configuration/view-configuration.query';
import {REPORT_MODAL_ID, ReportModal} from './zk/report-modal/report-modal.model';
import {ActivatedRoute, Router} from '@angular/router';
import proj4 from 'proj4';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {delay, map, switchMap, take} from 'rxjs/operators';
import {ConfigurationModal, SHARE_CONFIGURATION_MODAL_ID} from './zk/configuration/state/configuration.model';
import {resizeImage} from '../../utils/miscellaneous/miscellaneous';
import {LocationSearchResultsQuery} from './state/location-search-results/location-search-results.query';
import {animate, state, style, transition, trigger} from '@angular/animations';


@Component({
  selector: 's4e-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.scss'],
  animations: [
    trigger('sidebar', [
      state('open', style({
        left: '0'
      })),
      state('closed', style({
        left: '-400px'
      })),
      transition('open => closed', [
        animate('300ms ease-out')
      ]),
      transition('closed => open', [
        animate('300ms ease-out')
      ])
    ])
  ]
})
export class MapViewComponent implements OnInit, OnDestroy {
  public isMobileSidebarOpen = false;

  public loading$: Observable<boolean>;
  public activeScene$: Observable<Scene>;
  public activeSceneUrl$: Observable<string>;
  public activeProducts$: Observable<Product | null>;
  public timelineUI$: Observable<TimelineUI>;
  public scenesAreLoading$: Observable<boolean>;
  public legend$: Observable<Legend>;
  public legendState$: Observable<LegendState>;
  public userLoggedIn$: Observable<boolean>;
  public placeSearchResults$: Observable<LocationSearchResult[]>;
  public placeSearchLoading$: Observable<boolean>;
  public placeSearchResultsOpen$: Observable<boolean>;
  public activeView$: Observable<ViewPosition>;
  public currentTimelineDate$: Observable<string>;
  public availableDates$: Observable<string[]>;
  public showZKOptions$: Observable<boolean>;
  public showLoginOptions$: Observable<boolean>;
  public showProductDescription$: Observable<boolean>;
  public selectedLocation$: Observable<LocationSearchResult | null>;
  public overlays$: Observable<UIOverlay[]>;
  public userIsZK$: Observable<boolean>;
  public timelineResolution$: Observable<number>;

  @ViewChild('map', {read: MapComponent}) mapComponent: MapComponent;
  sidebarOpen$: Observable<boolean> = this.mapQuery.select('sidebarOpen');

  constructor(
    public mapService: MapService,
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
    private searchResultsQuery: LocationSearchResultsQuery,
    private modalService: ModalService,
    private viewConfigurationQuery: ViewConfigurationQuery
  ) {}

  ngOnInit(): void {
    this.userIsZK$ = this.sessionQuery.selectMemberZK();
    this.loading$ = this.mapQuery.selectLoading();
    this.currentTimelineDate$ = this.productQuery.selectSelectedDate();
    this.activeScene$ = this.sceneQuery.selectActive();
    this.activeSceneUrl$ = this.activeScene$
      .pipe(map((scene) => !!scene
        ? environment.apiPrefixV1 + '/scenes/' + scene.id + '/download'
        : null
      ));
    this.timelineUI$ = this.sceneQuery.selectTimelineUI();
    this.scenesAreLoading$ = this.sceneQuery.selectLoading();
    this.overlays$ = this.overlayQuery.selectVisibleAsUIOverlays();
    this.activeProducts$ = this.productQuery.selectActive();
    // TODO - uncomment it when the legends are properly seeded
    // this.legend$ = this.legendQuery.selectLegend();
    // For now we use the stub with static legend to get user's feedback
    this.legend$ = this.activeProducts$.pipe(map(product => product == null ? null : {
      type: 'gradient',
      url: '',
      bottomMetric: {},
      leftDescription: {},
      rightDescription: {},
      topMetric: {}
    } as Legend));

    this.legendState$ = this.legendQuery.select();
    this.placeSearchLoading$ = this.searchResultsQuery.selectLoading();
    this.placeSearchResults$ = this.searchResultsQuery.selectAll();
    this.placeSearchResultsOpen$ = this.searchResultsQuery.selectIsOpen();
    this.userLoggedIn$ = this.sessionQuery.isLoggedIn$();
    this.availableDates$ = this.productQuery.selectAvailableDates();
    this.showZKOptions$ = this.mapQuery.select('zkOptionsOpened');
    this.showLoginOptions$ = this.mapQuery.select('loginOptionsOpened');
    this.showProductDescription$ = this.mapQuery.select('productDescriptionOpened');
    this.timelineResolution$ = this.productQuery.selectTimelineResolution()
    this.sidebarOpen$.pipe(delay(0),untilDestroyed(this)).subscribe(() => this.mapComponent.updateSize())

    this.productService.get();
    this.overlayService.get();
    this.activeView$ = this.mapQuery.select('view');

    this.mapService.setView({
      centerCoordinates: proj4(environment.projection.toProjection, environment.projection.coordinates),
      zoomLevel: 6
    });

    this.mapService
      .connectRouterToStore(this.route)
      .pipe(
        untilDestroyed(this),
        switchMap(() => this.mapService.connectStoreToRouter())
      )
      .subscribe();

    this.productService.get();
    this.overlayService.get();
  }

  selectProduct(productId: number | null) {
    this.productService.toggleActive(productId);
  }

  selectScene(sceneId: number) {
    this.sceneService.setActive(sceneId);
  }

  toggleLegend() {
    this.legendService.toggleLegend();
  }

  setDate($event: string) {
    this.sceneService.get(this.productQuery.getActive(), $event, 'first');
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

  toggleProductDescription(show: boolean = true) {
    this.mapService.toggleProductDescription(show);
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
        id: REPORT_MODAL_ID,
        size: 'lg',
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
    const createUrl = (query) => this.router.createUrlTree([], {queryParams: query});
    const serializeUrl = (query) => this.router.serializeUrl(createUrl(query));
    const mapData$ = this.mapComponent
      .getMapData()
      .pipe(switchMap(data => resizeImage(data.image, 400, 247)));
    const path$ = this.mapQuery
      .selectQueryParamsFromStore()
      .pipe(
        take(1),
        map(query => serializeUrl(query))
      );
    combineLatest(mapData$, path$)
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

  ngOnDestroy(): void {}

  openJwtTokenModal() {
    this.modalService.show<Modal>({
      id: JWT_TOKEN_MODAL_ID,
      size: 'lg'
    });
    this.toggleZKOptions(false);
  }

  isLinkActive(url: string): boolean {
    return this.route.snapshot.children[0].url[0].path === url;
  }

  getLastAvailableScene() {
    this.productService.getLastAvailableScene();
  }

  nextScene() {
    this.productService.nextScene();
  }

  previousScene() {
    this.productService.previousScene();
  }

  nextDay() {
    this.productService.nextDay();
  }

  previousDay() {
    this.productService.previousDay();
  }

  increaseResolution() {
    this.productService.moveResolution(-1);
  }

  decreaseResolution() {
    this.productService.moveResolution(1);
  }
}
