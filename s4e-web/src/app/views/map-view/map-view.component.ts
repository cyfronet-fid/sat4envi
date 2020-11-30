import {EXPERT_HELP_MODAL_ID} from './zk/expert-help-modal/expert-help-modal.model';
import {JWT_TOKEN_MODAL_ID} from './jwt-token-modal/jwt-token-modal.model';
import {Modal} from '../../modal/state/modal.model';
import {environment} from 'src/environments/environment';
import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
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
import {Scene} from './state/scene/scene.model';
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
import {delay, filter, finalize, map, switchMap, take, tap} from 'rxjs/operators';
import {ConfigurationModal, SHARE_CONFIGURATION_MODAL_ID} from './zk/configuration/state/configuration.model';
import {resizeImage} from '../../utils/miscellaneous/miscellaneous';
import {LocationSearchResultsQuery} from './state/location-search-results/location-search-results.query';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {REPORT_TEMPLATES_MODAL_ID} from './zk/report-templates-modal/report-templates-modal.model';
import {ReportTemplateQuery} from './zk/state/report-templates/report-template.query';
import {ReportTemplateStore} from './zk/state/report-templates/report-template.store';
import {ViewConfigurationService} from './state/view-configuration/view-configuration.service';
import {filterFalse, filterNotNull} from '../../utils/rxjs/observable';


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
  public activeScene$: Observable<Scene> = this.sceneQuery.selectActive();
  public activeSceneUrl$: Observable<string> = this.activeScene$
    .pipe(map((scene) => !!scene
      ? environment.apiPrefixV1 + '/scenes/' + scene.id + '/download'
      : null
    ));

  public activeProducts$: Observable<Product | null> = this.productQuery.selectActive();
  public timelineUI$: Observable<TimelineUI> = this.sceneQuery.selectTimelineUI();
  public scenesAreLoading$: Observable<boolean> = this.sceneQuery.selectLoading();
  public legendState$: Observable<LegendState> = this.legendQuery.select();
  public userLoggedIn$: Observable<boolean> = this.sessionQuery.isLoggedIn$();
  public placeSearchResults$: Observable<LocationSearchResult[]> = this.searchResultsQuery.selectAll();
  public placeSearchLoading$: Observable<boolean> = this.searchResultsQuery.selectLoading();
  public placeSearchResultsOpen$: Observable<boolean> = this.searchResultsQuery.selectIsOpen();
  public activeView$: Observable<ViewPosition> = this.mapQuery.select('view');
  public currentTimelineDate$: Observable<string> = this.productQuery.selectSelectedDate();
  public availableDates$: Observable<string[]> = this.productQuery.selectAvailableDates();
  public showZKOptions$: Observable<boolean> = this.mapQuery.select('zkOptionsOpened');
  public showLoginOptions$: Observable<boolean> = this.mapQuery.select('loginOptionsOpened');
  public showProductDescription$: Observable<boolean> = this.mapQuery.select('productDescriptionOpened');
  public selectedLocation$: Observable<LocationSearchResult | null>;
  public overlays$: Observable<UIOverlay[]> = this.overlayQuery.selectVisibleAsUIOverlays();
  public userIsZK$: Observable<boolean> = this.sessionQuery.selectMemberZK();
  public timelineResolution$: Observable<number> = this.productQuery.selectTimelineResolution();
  public legend$ = this.legendQuery.selectLegend();
  public hasHeightContrast = this.viewConfigurationQuery.select('highContrast');
  public hasLargeFont = this.viewConfigurationQuery.select('largeFont');

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
    private viewConfigurationQuery: ViewConfigurationQuery,
    private reportTemplateQuery: ReportTemplateQuery,
    private reportTemplateStore: ReportTemplateStore,
    private viewConfigurationService: ViewConfigurationService,
  ) {
  }

  ngOnInit(): void {
    this.sidebarOpen$.pipe(delay(0), untilDestroyed(this)).subscribe(() => this.mapComponent.updateSize());

    this.mapService.setView({
      centerCoordinates: proj4(environment.projection.toProjection, environment.projection.coordinates),
      zoomLevel: 6
    });

    forkJoin([
      this.productService.get(),
      this.overlayService.get()
    ]).pipe(
      switchMap(() => this.mapService.loadMapQueryParams()),
      untilDestroyed(this),
      switchMap(() => this.mapService.connectStoreToRouter())
    ).subscribe();

    this.viewConfigurationQuery.selectLoading()
      .pipe(
        filterFalse(),
        map(() => this.viewConfigurationQuery.getActive()),
        filterNotNull(),
        switchMap(() => this._openShareViewModal$()),
        tap(() => this.viewConfigurationService.setActive(null))
      )
      .subscribe();

    this.reportTemplateQuery.selectActive()
      .pipe(
        untilDestroyed(this),
        filterNotNull()
      ).subscribe(() => this.openReportModal());
  }

  selectScene(sceneId: number) {
    this.sceneService.setActive(sceneId);
  }

  toggleLegend() {
    this.legendService.toggleLegend();
  }

  setDate($event: string) {
    this.sceneService.get(this.productQuery.getActive(), $event, 'first').subscribe();
    this.productService.setSelectedDate($event);
  }

  loadAvailableDates($event: string) {
    this.productService.fetchAvailableDays$($event).subscribe();
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

  toggleHighContrast() {
    this.viewConfigurationService.toggleHighContract();
  }

  toggleLargeFont() {
    this.viewConfigurationService.toggleLargeFont();
  }

  downloadMapImage() {
    this.mapComponent.downloadMap();
    this.toggleZKOptions(false);
  }

  openExpertHelpModal() {
    this.modalService.show<ReportModal>({
      id: EXPERT_HELP_MODAL_ID,
      size: 'lg'
    });
    this.toggleZKOptions(false);
  }

  openReportModal() {
    forkJoin([
      this.mapComponent.getMapData(),
      this.productQuery.selectActive().pipe(map(p => p == null ? null : p.name), take(1)),
      this.sceneQuery.selectActive().pipe(map(s => s == null ? null : s.timestamp), take(1))
    ])
      .pipe(
        untilDestroyed(this),
        filter(([mapData, productName, sceneDate]) => !!mapData)
      )
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
    this._openShareViewModal$().subscribe();
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

  openJwtTokenModal() {
    this.modalService.show<Modal>({
      id: JWT_TOKEN_MODAL_ID,
      size: 'lg'
    });
    this.toggleZKOptions(false);
  }

  openReportTemplatesModal() {
    this.modalService.show<Modal>({
      id: REPORT_TEMPLATES_MODAL_ID,
      size: 'lg'
    });
    this.toggleZKOptions(false);
  }

  isLinkActive(url: string): boolean {
    return this.route.snapshot.children[0].url[0].path === url;
  }

  getLastAvailableScene() {
    this.productService.getLastAvailableScene$()
      .pipe(untilDestroyed(this))
      .subscribe();
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

  private _openShareViewModal$() {
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
    return combineLatest(mapData$, path$)
      .pipe(
        tap(([mapData, path]) => this.modalService.show<ConfigurationModal>({
          id: SHARE_CONFIGURATION_MODAL_ID, size: 'lg',
          mapImage: mapData,
          configurationUrl: path
        })),
        finalize(() => this.toggleZKOptions(false))
      );
  }
}
