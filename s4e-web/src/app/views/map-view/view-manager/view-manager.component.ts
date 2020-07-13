import { SessionQuery } from './../../../state/session/session.query';
import { Component, OnDestroy, OnInit, ViewChild, ElementRef, AfterViewInit, AfterContentChecked, HostListener, Renderer2 } from '@angular/core';
import {IUILayer} from '../state/common.model';
import {LocationSearchResult} from '../state/location-search-results/location-search-result.model';
import {environment} from '../../../../environments/environment';
import { map, filter } from 'rxjs/operators';
import {combineLatest, Observable} from 'rxjs';
import {ProductQuery} from '../state/product/product.query';
import {OverlayQuery} from '../state/overlay/overlay.query';
import {MapQuery} from '../state/map/map.query';
import {SceneQuery} from '../state/scene/scene.query.service';
import {ProductService} from '../state/product/product.service';
import {OverlayService} from '../state/overlay/overlay.service';
import {SearchResultsService} from '../state/location-search-results/locations-search-results.service';
import { LocationSearchResultsQuery } from '../state/location-search-results/location-search-results.query';
import { ResizeEvent } from 'angular-resizable-element';
import { InjectorModule } from 'src/app/common/injector.module';

@Component({
  selector: 's4e-view-manager',
  templateUrl: './view-manager.component.html',
  styleUrls: ['./view-manager.component.scss']
})
export class ViewManagerComponent implements OnInit, OnDestroy {
  pickerRef: ElementRef = null;
  @ViewChild('picker', { read: ElementRef })
  set _pickerRef(pickerRef: ElementRef) {
    if (pickerRef) {
      // run after current change detection cycle
      setTimeout(() => this.pickerRef = pickerRef);
    }
  }
  loading$: Observable<boolean>;
  scenes: IUILayer[] = [];
  products$: Observable<IUILayer[]>;
  productsLoading$: Observable<boolean>;
  overlays$: Observable<IUILayer[]>;
  overlaysLoading$: Observable<boolean>;
  searchResults$: Observable<LocationSearchResult[]>;
  searchResultsLoading$: Observable<boolean>;
  searchResultsOpen$: Observable<boolean>;
  isFavouriteFiltration: boolean = false;
  searchValue: string;

  favouriteProductsCount$: Observable<number>;

  constructor(
    private productQuery: ProductQuery,
    private overlayQuery: OverlayQuery,
    private mapQuery: MapQuery,
    private sceneQuery: SceneQuery,
    private searchResultsQuery: LocationSearchResultsQuery,
    private productService: ProductService,
    private overlayService: OverlayService,
    private searchResultsService: SearchResultsService,
    private sessionQuery: SessionQuery,
    private _renderer: Renderer2
  ) {}

  ngOnInit(): void {
    this.overlaysLoading$ = this.overlayQuery.selectLoading();
    this.products$ = this.productQuery.selectAllFilteredAsUILayer();
    this.favouriteProductsCount$ = this.productQuery.selectFavouritesCount();
    this.productsLoading$ = this.productQuery.selectLoading();
    this.searchResultsLoading$ = this.searchResultsQuery.selectLoading();
    this.overlays$ = this.overlayQuery.selectAllAsUIOverlays();
    this.searchResults$ = this.searchResultsQuery.selectAll();
    this.searchResultsOpen$ = this.searchResultsQuery.selectIsOpen();

    this.productQuery.selectIsFavouriteMode().subscribe(isFavourite => this.isFavouriteFiltration = isFavourite);

    this.loading$ = combineLatest([
      this.overlaysLoading$,
      this.productsLoading$
    ]).pipe(map(([overlayLoading, productsLoading]) => overlayLoading || productsLoading));

    if (environment.hmr) {
      const location = this.searchResultsQuery.getValue().searchResult;
      this.searchValue = !!location && location.name || '';
    }
  }

  get isLoggedIn() {
    return this.sessionQuery.isLoggedIn();
  }

  ngOnDestroy(): void {
  }

  toggleSearchResult(show: boolean) {
    InjectorModule.Injector.get(SearchResultsService).toggleSearchResults(show);
  }

  selectProduct(productId: number) {
    if(this.productQuery.getActiveId() === productId) {
      productId = null;
    }

    this.productService.setActive(productId);
  }

  selectOverlay(overlayId: string) {
    this.overlayService.setActive(overlayId);
  }

  isFavouriteProduct = (ID: number, isFavourite: boolean): boolean => {
    this.productService.toggleFavourite(ID, isFavourite);
    return false;
  }

  searchForPlaces(place: string) {
    this.searchResultsService.get(place);
  }

  navigateToPlace(place: LocationSearchResult) {
    this.searchResultsService.setSelectedPlace(place);
    this.searchValue = place.name;
  }

  selectFirstResult() {
    const firstSearchResult = this.searchResultsQuery.getAll()[0];
    if(!!firstSearchResult) {
      this.navigateToPlace(firstSearchResult);
    }
  }

  resetSearch() {
    this.searchValue = '';
    this.searchResultsService.setSelectedPlace(null);
  }

  setViewModeToFavourite(favourite: boolean) {
    this.productService.setFavouriteMode(favourite)
  }

  onResizeEnd(event: ResizeEvent) {
    const MAX_HEIGHT = 450;
    const MIN_HEIGHT = 0;
    const OFFSET = -30;
    const sign = Math.sign(event.edges.top as number);
    const calculatedHeight = this.pickerRef.nativeElement.offsetHeight - (event.edges.top as number) + sign * OFFSET;

    let height = calculatedHeight > MIN_HEIGHT && calculatedHeight < MAX_HEIGHT ? calculatedHeight : MIN_HEIGHT;
    height = calculatedHeight < MIN_HEIGHT ? MIN_HEIGHT : height;
    height = calculatedHeight > MAX_HEIGHT ? MAX_HEIGHT : height;

    this._renderer.setStyle(this.pickerRef.nativeElement, 'height', `${height as number}px`);
  }
}
