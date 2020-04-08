import {Component, OnDestroy, OnInit} from '@angular/core';
import {IUILayer} from '../state/common.model';
import {FormControl} from '@ng-stack/forms';
import {SearchResult} from '../state/search-results/search-result.model';
import {SearchResultsQuery} from '../state/search-results/search-results.query';
import {environment} from '../../../../environments/environment';
import {debounceTime, distinctUntilChanged, map} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {combineLatest, Observable} from 'rxjs';
import {ProductQuery} from '../state/product/product.query';
import {OverlayQuery} from '../state/overlay/overlay.query';
import {MapQuery} from '../state/map/map.query';
import {SceneQuery} from '../state/scene/scene.query.service';
import {ProductService} from '../state/product/product.service';
import {OverlayService} from '../state/overlay/overlay.service';
import {SearchResultsService} from '../state/search-results/search-results.service';

@Component({
  selector: 's4e-view-manager',
  templateUrl: './view-manager.component.html',
  styleUrls: ['./view-manager.component.scss'],
})
export class ViewManagerComponent implements OnInit, OnDestroy{
  loading$: Observable<boolean>;
  scenes: IUILayer[] = [];
  products$: Observable<IUILayer[]>;
  productsLoading$: Observable<boolean>;
  overlays$: Observable<IUILayer[]>;
  overlaysLoading$: Observable<boolean>;
  searchResults$: Observable<SearchResult[]>;
  searchResultsLoading$: Observable<boolean>;
  searchResultsOpen$: Observable<boolean>;
  searchFc: FormControl<string> = new FormControl<string>('');

  constructor(private searchResultQuery: SearchResultsQuery,
              private productQuery: ProductQuery,
              private overlayQuery: OverlayQuery,
              private mapQuery: MapQuery,
              private sceneQuery: SceneQuery,
              private searchResultsQuery: SearchResultsQuery,
              private productService: ProductService,
              private overlayService: OverlayService,
              private searchResultsService: SearchResultsService) {
  }

  ngOnInit(): void {
    this.overlaysLoading$ = this.overlayQuery.selectLoading();
    this.products$ = this.productQuery.selectAllAsUILayer();
    this.productsLoading$ = this.productQuery.selectLoading();
    this.searchResultsLoading$ = this.searchResultsQuery.selectLoading();
    this.overlays$ = this.overlayQuery.selectAllAsUIOverlays();
    this.searchResults$ = this.searchResultsQuery.selectAll();
    this.searchResultsOpen$ = this.searchResultsQuery.selectIsOpen();
    this.loading$ = combineLatest([
      this.overlaysLoading$,
      this.productsLoading$
    ]).pipe(map(([overlayLoading, productsLoading]) => overlayLoading || productsLoading));

    if (environment.hmr) {
      this.searchFc.setValue(this.searchResultQuery.getValue().queryString);
    }

    this.searchFc.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      untilDestroyed(this),
    ).subscribe((text: string) => this.searchForPlaces(text));
  }

  resetSelectedLocation() {
    this.searchForPlaces('');
    this.searchFc.setValue('');
  }

  ngOnDestroy(): void {
  }

  selectProduct(productId: number | null) {
    this.productService.setActive(productId);
  }

  selectOverlay(overlayId: string) {
    this.overlayService.setActive(overlayId);
  }

  searchForPlaces(place: string) {
    this.searchResultsService.get(place);
  }

  navigateToPlace(place: SearchResult) {
    this.searchResultsService.setSelectedPlace(place);
  }

  selectFirstResult() {
    this.searchResultsService.setFirstAsSelectedPlace()
  }
}
