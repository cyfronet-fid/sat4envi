/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import {LegendState} from './state/legend/legend.model';
import {LegendQuery} from './state/legend/legend.query';
import {LegendService} from './state/legend/legend.service';
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
import {
  SAVE_CONFIG_MODAL_ID,
  SaveConfigModal
} from './zk/save-config-modal/save-config-modal.model';
import {
  LIST_CONFIGS_MODAL_ID,
  ListConfigsModal
} from './zk/list-configs-modal/list-configs-modal.model';
import {ViewConfigurationQuery} from './state/view-configuration/view-configuration.query';
import {REPORT_MODAL_ID, ReportModal} from './zk/report-modal/report-modal.model';
import {ActivatedRoute, Router} from '@angular/router';
import proj4 from 'proj4';
import {delay, filter, finalize, map, switchMap, take, tap} from 'rxjs/operators';
import {
  ConfigurationModal,
  SHARE_CONFIGURATION_MODAL_ID
} from './zk/configuration/state/configuration.model';
import {resizeImage} from '../../utils/miscellaneous/miscellaneous';
import {LocationSearchResultsQuery} from './state/location-search-results/location-search-results.query';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {REPORT_TEMPLATES_MODAL_ID} from './zk/report-templates-modal/report-templates-modal.model';
import {ReportTemplateQuery} from './zk/state/report-templates/report-template.query';
import {ReportTemplateStore} from './zk/state/report-templates/report-template.store';
import {ViewConfigurationService} from './state/view-configuration/view-configuration.service';
import {filterFalse, filterNotNull, mapAnyTrue} from '../../utils/rxjs/observable';
import {MOBILE_MODAL_SCENE_SELECTOR_MODAL_ID} from './timeline/mobile-scene-selector-modal/mobile-scene-selector-modal.model';
import {SentinelSearchQuery} from './state/sentinel-search/sentinel-search.query';
import {InstitutionService} from '../settings/state/institution/institution.service';
import {InstitutionQuery} from '../settings/state/institution/institution.query';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {SentinelSearchService} from './state/sentinel-search/sentinel-search.service';

@UntilDestroy()
@Component({
  selector: 's4e-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.scss'],
  animations: [
    trigger('sidebar', [
      state(
        'open',
        style({
          left: '0'
        })
      ),
      state(
        'closed',
        style({
          left: '-400px'
        })
      ),
      transition('open => closed', [animate('300ms ease-out')]),
      transition('closed => open', [animate('300ms ease-out')])
    ])
  ]
})
export class MapViewComponent implements OnInit, OnDestroy {
  public isMobileSidebarOpen = false;
  public activeScene$: Observable<Scene> = this.sceneQuery.selectActive();
  public activeProducts$: Observable<Product | null> = this.productQuery.selectActive();
  public timelineUI$: Observable<TimelineUI> = this.sceneQuery.selectTimelineUI();
  public scenesAreLoading$: Observable<boolean> = this.sceneQuery.selectLoading();
  public legendState$: Observable<LegendState> = this.legendQuery.select();
  public userLoggedIn$: Observable<boolean> = this.sessionQuery.isLoggedIn$();
  public activeView$: Observable<ViewPosition> = this.mapQuery.select('view');
  public currentTimelineDate$: Observable<string> = this.productQuery.selectSelectedDate();
  public datesEnabled$: Observable<
    Date[]
  > = this.productQuery.selectAvailableDates();
  public showLoginOptions$: Observable<boolean> = this.mapQuery.select(
    'loginOptionsOpened'
  );
  public showProductDescription$: Observable<boolean> = this.mapQuery.selectShowProductDescription();
  public overlays$: Observable<
    UIOverlay[]
  > = this.overlayQuery.selectVisibleAsUIOverlays();
  public userIsAuthorizedForAdditionalFunctionalities$: Observable<boolean> = combineLatest(
    [this.sessionQuery.selectPakMember(), this.sessionQuery.selectMemberZK()]
  ).pipe(mapAnyTrue());
  public timelineResolution$: Observable<number> = this.productQuery.selectTimelineResolution();
  public legend$ = this.legendQuery.selectLegend();
  public hasHeightContrast$ = this.viewConfigurationQuery.select('highContrast');
  public hasLargeFont$ = this.viewConfigurationQuery.select('largeFont');
  public areSentinelSearchResultsOpen$ = this.sentinelSearchQuery.selectShowSearchResults();
  public isAdminOfOneInstitution$ = this.institutionQuery.selectHasOnlyOneAdministrationInstitution();
  public hasAnyAdminInstitution$ = this.institutionQuery
    .selectAdministrationInstitutions$()
    .pipe(map(institutions => !!institutions && institutions.length > 0));

  public cookiePolicyAccepted$ = this.sessionQuery.selectCookiePolicyAccepted();

  @ViewChild('map', {read: MapComponent, static: true}) mapComponent: MapComponent;
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
    private sentinelSearchQuery: SentinelSearchQuery,
    private institutionQuery: InstitutionQuery,
    private institutionService: InstitutionService,
    private sentinelSearchService: SentinelSearchService
  ) {}

  ngOnInit(): void {
    this.sidebarOpen$
      .pipe(delay(0), untilDestroyed(this))
      .subscribe(() => this.mapComponent.updateSize());

    this.mapService.setView({
      centerCoordinates: proj4(
        environment.projection.toProjection,
        environment.projection.coordinates
      ),
      zoomLevel: 6
    });

    this.mapService
      .loadMapQueryParams()
      .pipe(
        untilDestroyed(this),
        switchMap(() =>
          forkJoin([this.productService.get(), this.overlayService.get()])
        ),
        switchMap(() =>
          this.mapService
            .connectStoreToRouter()
            // IMPORTANT!! Due to navigations this switch map is never destroyed
            // , so it must to be done in here
            .pipe(untilDestroyed(this))
        ),
        switchMap(() => this.sceneService.connectQueryToDetailsModal$())
      )
      .subscribe();

    this.viewConfigurationQuery
      .selectLoading()
      .pipe(
        filterFalse(),
        map(() => this.viewConfigurationQuery.getActive()),
        filterNotNull(),
        switchMap(() => this._openShareViewModal$()),
        tap(() => this.viewConfigurationService.setActive(null))
      )
      .subscribe();

    this.reportTemplateQuery
      .selectActive()
      .pipe(untilDestroyed(this), filterNotNull())
      .subscribe(() => this.openReportModal());

    if (this.sessionQuery.isLoggedIn()) {
      this.institutionService.get();
    }
  }

  selectScene(sceneId: number) {
    this.sceneService.setActive(sceneId, true);
  }

  toggleLegend() {
    this.legendService.toggleLegend();
  }

  setDate($event: string) {
    this.sceneService
      .get(this.productQuery.getActive(), $event, 'first')
      .subscribe();
    this.productService.setSelectedDate($event, true);
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
      this.productQuery.selectActive().pipe(take(1)),
      this.sceneQuery.selectActive().pipe(
        map(s => (s == null ? null : s.timestamp)),
        take(1)
      )
    ])
      .pipe(
        untilDestroyed(this),
        filter(([mapData, product, sceneDate]) => !!mapData)
      )
      .subscribe(([mapData, product, sceneDate]) =>
        this.modalService.show<ReportModal>({
          id: REPORT_MODAL_ID,
          size: 'lg',
          image: mapData,
          productName: product == null ? '' : product.displayName,
          legend: product == null ? null : product.legend,
          sceneDate: sceneDate
        })
      );
    this.toggleZKOptions(false);
  }

  openSaveViewModal() {
    this.mapComponent
      .getMapData()
      .pipe(switchMap(data => resizeImage(data.image, 170, 105)))
      .subscribe(mapData =>
        this.modalService.show<SaveConfigModal>({
          id: SAVE_CONFIG_MODAL_ID,
          size: 'lg',
          viewConfiguration: {
            ...this.viewConfigurationQuery.getCurrent(),
            thumbnail: mapData
          }
        })
      );
    this.toggleZKOptions(false);
  }

  openShareViewModal() {
    this._openShareViewModal$()
      .pipe(
        untilDestroyed(this),
        tap(() => console.trace())
      )
      .subscribe();
  }

  viewChanged($event: ViewPosition) {
    this.mapService.setView($event);
  }

  openListViewModal() {
    this.modalService.show<ListConfigsModal>({
      id: LIST_CONFIGS_MODAL_ID,
      size: 'lg'
    });
    this.toggleZKOptions(false);
  }

  ngOnDestroy(): void {
    this.sentinelSearchService.clearMetadata();
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
    this.productService.getLastAvailableScene$(true).subscribe();
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
    const createUrl = query => this.router.createUrlTree([], {queryParams: query});
    const serializeUrl = query => this.router.serializeUrl(createUrl(query));
    const mapData$ = this.mapComponent
      .getMapData()
      .pipe(switchMap(data => resizeImage(data.image, 400, 247)));
    const path$ = this.mapQuery.selectQueryParamsFromStore().pipe(
      take(1),
      map(query => serializeUrl(query))
    );
    return combineLatest(mapData$, path$).pipe(
      tap(([mapData, path]) =>
        this.modalService.show<ConfigurationModal>({
          id: SHARE_CONFIGURATION_MODAL_ID,
          size: 'lg',
          mapImage: mapData,
          configurationUrl: path
        })
      ),
      finalize(() => this.toggleZKOptions(false))
    );
  }

  showDetailsModal() {
    this.sceneService.showModalForActive();
  }

  openSceneSelectionModal() {
    this.modalService.show({id: MOBILE_MODAL_SCENE_SELECTOR_MODAL_ID});
  }

  acceptCookies() {
    this.sessionService.acceptCookiePolicy();
  }
}
