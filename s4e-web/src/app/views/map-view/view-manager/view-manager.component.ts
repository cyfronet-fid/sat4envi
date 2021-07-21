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

import {LocationSearchResultsStore} from '../state/location-search-results/locations-search-results.store';
import {SessionQuery} from '../../../state/session/session.query';
import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  Renderer2,
  ViewChild
} from '@angular/core';
import {IUILayer} from '../state/common.model';
import {LocationSearchResult} from '../state/location-search-results/location-search-result.model';
import {environment} from '../../../../environments/environment';
import {combineLatest, Observable} from 'rxjs';
import {ProductQuery} from '../state/product/product.query';
import {OverlayQuery} from '../state/overlay/overlay.query';
import {ProductService} from '../state/product/product.service';
import {OverlayService} from '../state/overlay/overlay.service';
import {SearchResultsService} from '../state/location-search-results/locations-search-results.service';
import {LocationSearchResultsQuery} from '../state/location-search-results/location-search-results.query';
import {ResizeEvent} from 'angular-resizable-element';
import {ModalService} from '../../../modal/state/modal.service';
import {OVERLAY_LIST_MODAL_ID} from './overlay-list-modal/overlay-list-modal.model';
import {TimelineService} from '../state/scene/timeline.service';
import {mapAnyTrue} from '../../../utils/rxjs/observable';
import {UntilDestroy} from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 's4e-view-manager',
  templateUrl: './view-manager.component.html',
  styleUrls: ['./view-manager.component.scss']
})
export class ViewManagerComponent implements OnInit, OnDestroy {
  pickerRef: ElementRef = null;

  @ViewChild('picker', {read: ElementRef})
  set _pickerRef(pickerRef: ElementRef) {
    if (pickerRef) {
      // run after current change detection cycle
      setTimeout(() => (this.pickerRef = pickerRef));
    }
  }

  scenes: IUILayer[] = [];
  groupedProducts$: Observable<
    IUILayer[][]
  > = this.productQuery.selectGroupedProducts();
  productsLoading$: Observable<boolean> = this.productQuery.selectLoading();
  overlays$: Observable<IUILayer[]> = this.overlayQuery.selectVisibleAsUIOverlays();
  overlaysLoading$: Observable<boolean> = this.overlayQuery.selectLoading();
  loading$: Observable<boolean> = combineLatest([
    this.overlaysLoading$,
    this.productsLoading$
  ]).pipe(mapAnyTrue());

  isFavouriteFiltration: boolean = false;
  searchValue: string;

  favouriteProductsCount$: Observable<number> = this.productQuery.selectFavouritesCount();

  spacerHeight: number = 231;

  constructor(
    public searchResultsQuery: LocationSearchResultsQuery,
    public searchResultsStore: LocationSearchResultsStore,
    public productService: ProductService,

    private productQuery: ProductQuery,
    private overlayQuery: OverlayQuery,
    private overlayService: OverlayService,
    private searchResultsService: SearchResultsService,
    private sessionQuery: SessionQuery,
    private _renderer: Renderer2,
    private _modalService: ModalService,
    private timelineService: TimelineService
  ) {}

  ngOnInit(): void {
    this.productQuery
      .selectIsFavouriteMode()
      .subscribe(isFavourite => (this.isFavouriteFiltration = isFavourite));

    if (environment.hmr) {
      const location = this.searchResultsQuery.getValue().searchResult;
      this.searchValue = (!!location && location.name) || '';
      if (!!location) {
        this.navigateToPlace(location);
      }
    }
  }

  get isLoggedIn() {
    return this.sessionQuery.isLoggedIn();
  }

  ngOnDestroy(): void {}

  toggleSearchResult(show: boolean) {
    this.searchResultsService.toggleSearchResults(show);
  }

  async selectProduct(productId: number) {
    const turnOfLiveMode = await this.timelineService.confirmTurningOffLiveMode();
    if (!turnOfLiveMode) {
      return;
    }

    if (this.productQuery.getActiveId() === productId) {
      productId = null;
    }

    this.productService.setActive$(productId).subscribe();
  }

  selectOverlay(overlayId: number) {
    this.overlayService.setActive(overlayId);
  }

  isFavouriteProduct = (ID: number, isFavourite: boolean): boolean => {
    this.productService.toggleFavourite(ID, isFavourite);
    return false;
  };

  searchForPlaces(place: string) {
    if (!place || place === '') {
      this.searchResultsService.setSelectedPlace(null);
      this.searchResultsService.get('a');
      return;
    }

    this.searchResultsService.get(place);
  }

  navigateToPlace(place: LocationSearchResult) {
    this.searchResultsService.setSelectedPlace(place);
    this.searchValue = place.name;
  }

  setViewModeToFavourite(favourite: boolean) {
    this.productService.setFavouriteMode(favourite);
  }

  onResizeEnd(event: ResizeEvent) {
    const MAX_HEIGHT = 450;
    const MIN_HEIGHT = 0;
    const OFFSET = -30;
    const sign = Math.sign(event.edges.top as number);
    const calculatedHeight =
      this.pickerRef.nativeElement.offsetHeight -
      (event.edges.top as number) +
      sign * OFFSET;

    let height =
      calculatedHeight > MIN_HEIGHT && calculatedHeight < MAX_HEIGHT
        ? calculatedHeight
        : MIN_HEIGHT;
    height = calculatedHeight < MIN_HEIGHT ? MIN_HEIGHT : height;
    height = calculatedHeight > MAX_HEIGHT ? MAX_HEIGHT : height;

    this.spacerHeight = height + 50;
    this._renderer.setStyle(
      this.pickerRef.nativeElement,
      'height',
      `${height as number}px`
    );
  }

  showOverlayListModal() {
    this._modalService.show({id: OVERLAY_LIST_MODAL_ID, size: 'lg'});
  }
}
