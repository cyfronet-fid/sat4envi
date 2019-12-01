import {Component, OnInit, ViewChild} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {UIOverlay} from './state/overlay/overlay.model';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {OverlayQuery} from './state/overlay/overlay.query';

import {OverlayService} from './state/overlay/overlay.service';
import {IUILayer} from './state/common.model';
import {S4eConfig} from '../../utils/initializer/config.service';
import {MapState, ViewPosition} from './state/map/map.model';
import {Legend, LegendState} from './state/legend/legend.model';
import {LegendQuery} from './state/legend/legend.query';
import {LegendService} from './state/legend/legend.service';
import {SearchResult} from './state/search-results/search-result.model';
import {SearchResultsQuery} from './state/search-results/search-results.query';
import {SearchResultsService} from './state/search-results/search-results.service';
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

@Component({
  selector: 's4e-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.scss'],
})
export class MapViewComponent implements OnInit {
  productsTypeList$: Observable<IUILayer[]>;

  overlays$: Observable<UIOverlay[]>;

  public loading$: Observable<boolean>;
  public activeScene$: Observable<Scene>;
  public activeProducts$: Observable<Product | null>;
  public scenes$: Observable<Scene[]>;
  public scenesAreLoading$: Observable<boolean>;
  public viewManagerLoading$: Observable<boolean>;
  public productLoading$: Observable<boolean>;
  public mapState$: Observable<MapState>;
  public legend$: Observable<Legend>;
  public legendState$: Observable<LegendState>;
  public overlaysLoading$: Observable<boolean>;
  public activeOverlays$: Observable<UIOverlay[]>;
  public userLoggedIn$: Observable<boolean>;
  public placeSearchResults$: Observable<SearchResult[]>;
  public placeSearchLoading$: Observable<boolean>;
  public placeSearchResultsOpen$: Observable<boolean>;
  public selectedLocation$: Subject<SearchResult> = new Subject<SearchResult>();
  public currentTimelineDate$: Observable<string>;
  public availableDates$: Observable<string[]>;
  public showZKOptions$: Observable<boolean>;
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
              private searchResultsService: SearchResultsService,
              private searchResultsQuery: SearchResultsQuery,
              private modalService: ModalService,
              private CONFIG: S4eConfig) {
  }

  ngOnInit(): void {
    // this.viewManagerLoading$ = combineLatest(this.recentViewQuery.selectLoading(), this.overlayQuery.selectLoading())
    //   .pipe(map(values => values.reduce((prev, curr) => prev || curr)));
    this.loading$ = this.mapQuery.selectLoading();
    this.productsTypeList$ = this.productQuery.selectAllAsUILayer();
    this.currentTimelineDate$ = this.productQuery.selectSelectedDate();
    this.activeScene$ = this.sceneQuery.selectActive();
    this.scenes$ = this.sceneQuery.selectAll();
    this.scenesAreLoading$ = this.sceneQuery.selectLoading();
    this.overlays$ = this.overlayQuery.selectAllAsUIOverlays();
    this.activeOverlays$ = this.overlayQuery.selectActiveUIOverlays();
    this.overlaysLoading$ = this.overlayQuery.selectLoading();
    this.productLoading$ = this.productQuery.selectLoading();
    this.mapState$ = this.mapQuery.select();
    this.activeProducts$ = this.productQuery.selectActive();
    this.legend$ = this.legendQuery.selectLegend();
    this.legendState$ = this.legendQuery.select();
    this.placeSearchLoading$ = this.searchResultsQuery.selectLoading();
    this.placeSearchResults$ = this.searchResultsQuery.selectAll();
    this.placeSearchResultsOpen$ = this.searchResultsQuery.selectIsOpen();
    this.productService.get();
    this.overlayService.get();
    this.userLoggedIn$ = this.sessionQuery.isLoggedIn$();
    this.availableDates$ = this.productQuery.selectAvailableDates();
    this.showZKOptions$ = this.mapQuery.select('zkOptionsOpened');
  }

  selectProduct(productId: number | null) {
    this.productService.setActive(productId);
  }

  selectScene(sceneId: number) {
    this.sceneService.setActive(sceneId);
  }

  selectOverlay(overlayId: string) {
      this.overlayService.setActive(overlayId);
  }

  logout() {
    this.sessionService.logout();
  }

  toggleLegend() {
    this.legendService.toggleLegend();
  }

  serachForPlaces(place: string) {
    this.searchResultsService.get(place);
  }

  navigateToPlace(place: SearchResult) {
    this.selectedLocation$.next(place);
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
  }

  openReportModal() {
    this.modalService.alert('Not Implemented', 'This feature is not yet implemented');
  }

  openSaveViewModal() {
    this.modalService.alert('Not Implemented', 'This feature is not yet implemented');
  }

  openShareViewModal() {
    this.modalService.alert('Not Implemented', 'This feature is not yet implemented');
  }

 viewChanged($event: ViewPosition) {
    this.mapService.setView($event);
  }
}
