import {Component, Inject, OnInit} from '@angular/core';
import {combineLatest, Observable, of} from 'rxjs';

import {Overlay, UIOverlay} from './state/overlay/overlay.model';
import {IConstants, S4E_CONSTANTS} from '../../app.constants';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {Product} from './state/product/product.model';
import {ProductService} from './state/product/product.service';
import {ProductQuery} from './state/product/product.query';
import {RecentViewQuery} from './state/recent-view/recent-view.query';
import {GranuleQuery} from './state/granule/granule.query';
import {Granule} from './state/granule/granule.model';
import {RecentView} from './state/recent-view/recent-view.model';
import {RecentViewService} from './state/recent-view/recent-view.service';
import {GranuleService} from './state/granule/granule.service';
import {OverlayQuery} from './state/overlay/overlay.query';
import {map} from 'rxjs/operators';
import {OverlayService} from './state/overlay/overlay.service';

@Component({
  selector: 's4e-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.scss'],
})
export class MapViewComponent implements OnInit {
  products$: Observable<Product[]>;

  overlays$: Observable<UIOverlay[]>;

  public loading$: Observable<boolean>;
  public activeGranule$: Observable<Granule>;
  public activeProduct$: Observable<Product>;
  public activeRecentView$: Observable<RecentView>;
  public recentViews$: Observable<RecentView[]>;
  public granules$: Observable<Granule[]>;
  public granulesAreLoading$: Observable<boolean>;
  public viewManagerLoading$: Observable<boolean>;
  public productLoading$: Observable<boolean>;

  constructor(private mapService: MapService,
              private mapQuery: MapQuery,
              private overlayQuery: OverlayQuery,
              private overlayService: OverlayService,
              private granuleService: GranuleService,
              private recentViewService: RecentViewService,
              private productService: ProductService,
              private productQuery: ProductQuery,
              private recentViewQuery: RecentViewQuery,
              private granuleQuery: GranuleQuery,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants) { }

  ngOnInit(): void {
    this.activeRecentView$ = this.recentViewQuery.selectActive();
    this.recentViews$ = this.recentViewQuery.selectViewsWithData();


    this.viewManagerLoading$ = combineLatest(this.recentViewQuery.selectLoading(), this.overlayQuery.selectLoading())
      .pipe(map(values => values.reduce((prev, curr) => prev || curr)));
    this.loading$ = this.mapQuery.selectLoading();
    this.products$ = this.productQuery.selectAll();
    this.activeGranule$ = this.recentViewQuery.selectActiveGranule();
    this.granules$ = this.recentViewQuery.selectActiveViewGranules();
    this.granulesAreLoading$ = this.granuleQuery.selectLoading();
    this.overlays$ = this.overlayQuery.selectAllAsUIOverlays();
    this.productLoading$ = this.productQuery.selectLoading();

    this.productService.get();
    this.overlayService.get();
  }

  selectProduct(productId: number|null) {
    this.productService.setActive(productId);
  }

  removeActiveView(viewId: number) {
    this.recentViewService.remove(viewId);
  }

  setActiveView(viewId: number) {
    this.recentViewService.setActive(viewId);
  }

  selectGranule(granuleId: number) {
    this.recentViewService.updateActiveViewGranule(granuleId);
  }
}
