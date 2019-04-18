import {Component, Inject, OnInit} from '@angular/core';
import {combineLatest, Observable, of} from 'rxjs';

import {Overlay, UIOverlay} from './state/overlay/overlay.model';
import {IConstants, S4E_CONSTANTS} from '../../app.constants';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {ProductType} from './state/product-type/product-type.model';
import {ProductTypeService} from './state/product-type/product-type.service';
import {ProductTypeQuery} from './state/product-type/product-type-query.service';
import {RecentViewQuery} from './state/recent-view/recent-view.query';
import {ProductQuery} from './state/product/product-query.service';
import {Product} from './state/product/product.model';
import {RecentView} from './state/recent-view/recent-view.model';
import {RecentViewService} from './state/recent-view/recent-view.service';
import {ProductService} from './state/product/product.service';
import {OverlayQuery} from './state/overlay/overlay.query';
import {map} from 'rxjs/operators';
import {OverlayService} from './state/overlay/overlay.service';

@Component({
  selector: 's4e-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.scss'],
})
export class MapViewComponent implements OnInit {
  products$: Observable<ProductType[]>;

  overlays$: Observable<UIOverlay[]>;

  public loading$: Observable<boolean>;
  public activeProduct$: Observable<Product>;
  public activeProductType$: Observable<ProductType>;
  public activeRecentView$: Observable<RecentView>;
  public recentViews$: Observable<RecentView[]>;
  public products: Observable<Product[]>;
  public productsAreLoading$: Observable<boolean>;
  public viewManagerLoading$: Observable<boolean>;
  public productTypeLoading$: Observable<boolean>;

  constructor(private mapService: MapService,
              private mapQuery: MapQuery,
              private overlayQuery: OverlayQuery,
              private overlayService: OverlayService,
              private productService: ProductService,
              private recentViewService: RecentViewService,
              private productTypeService: ProductTypeService,
              private productTypeQuery: ProductTypeQuery,
              private recentViewQuery: RecentViewQuery,
              private productQuery: ProductQuery,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants) { }

  ngOnInit(): void {
    this.activeRecentView$ = this.recentViewQuery.selectActive();
    this.recentViews$ = this.recentViewQuery.selectViewsWithData();


    this.viewManagerLoading$ = combineLatest(this.recentViewQuery.selectLoading(), this.overlayQuery.selectLoading())
      .pipe(map(values => values.reduce((prev, curr) => prev || curr)));
    this.loading$ = this.mapQuery.selectLoading();
    this.products$ = this.productTypeQuery.selectAll();
    this.activeProduct$ = this.recentViewQuery.selectActiveProduct();
    this.products = this.recentViewQuery.selectActiveViewProducts();
    this.productsAreLoading$ = this.productQuery.selectLoading();
    this.overlays$ = this.overlayQuery.selectAllAsUIOverlays();
    this.productTypeLoading$ = this.productTypeQuery.selectLoading();

    this.productTypeService.get();
    this.overlayService.get();
  }

  selectProductType(productId: number|null) {
    this.productTypeService.setActive(productId);
  }

  removeActiveView(viewId: number) {
    this.recentViewService.remove(viewId);
  }

  setActiveView(viewId: number) {
    this.recentViewService.setActive(viewId);
  }

  selectProduct(granuleId: number) {
    this.recentViewService.updateActiveViewProduct(granuleId);
  }
}