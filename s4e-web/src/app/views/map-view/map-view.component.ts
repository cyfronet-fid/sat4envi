import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {UIOverlay} from './state/overlay/overlay.model';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {ProductType} from './state/product-type/product-type.model';
import {ProductTypeService} from './state/product-type/product-type.service';
import {ProductTypeQuery} from './state/product-type/product-type.query';
import {ProductQuery} from './state/product/product.query';
import {Product} from './state/product/product.model';
import {ProductService} from './state/product/product.service';
import {OverlayQuery} from './state/overlay/overlay.query';
import {OverlayService} from './state/overlay/overlay.service';
import {IUILayer} from './state/common.model';
import {S4eConfig} from '../../utils/initializer/config.service';
import {SessionQuery} from '../../state/session/session.query';
import {SessionService} from '../../state/session/session.service';
import {MapState} from './state/map/map.model';
import {Legend, LegendState} from './state/legend/legend.model';
import {LegendQuery} from './state/legend/legend.query';
import {LegendService} from './state/legend/legend.service';

@Component({
  selector: 's4e-map-view',
  templateUrl: './map-view.component.html',
  styleUrls: ['./map-view.component.scss'],
})
export class MapViewComponent implements OnInit {
  productsTypeList$: Observable<IUILayer[]>;

  overlays$: Observable<UIOverlay[]>;

  public loading$: Observable<boolean>;
  public activeProduct$: Observable<Product>;
  public activeProductType$: Observable<ProductType|null>;
  public products$: Observable<Product[]>;
  public productsAreLoading$: Observable<boolean>;
  public viewManagerLoading$: Observable<boolean>;
  public productTypeLoading$: Observable<boolean>;
  public mapState$: Observable<MapState>;
  public legend$: Observable<Legend>;
  public legendState$: Observable<LegendState>;
  public userLoggedIn$: Observable<boolean>;

  constructor(private mapService: MapService,
              private mapQuery: MapQuery,
              private overlayQuery: OverlayQuery,
              private overlayService: OverlayService,
              private productService: ProductService,
              private productTypeService: ProductTypeService,
              private productTypeQuery: ProductTypeQuery,
              private productQuery: ProductQuery,
              private sessionQuery: SessionQuery,
              private sessionService: SessionService,
              private legendQuery: LegendQuery,
              private legendService: LegendService,
              private CONFIG: S4eConfig) {
  }

  ngOnInit(): void {
    // this.viewManagerLoading$ = combineLatest(this.recentViewQuery.selectLoading(), this.overlayQuery.selectLoading())
    //   .pipe(map(values => values.reduce((prev, curr) => prev || curr)));
    this.loading$ = this.mapQuery.selectLoading();
    this.productsTypeList$ = this.productTypeQuery.selectAllAsUILayer();
    this.activeProduct$ = this.productQuery.selectActive();
    this.products$ = this.productQuery.selectAll();
    this.productsAreLoading$ = this.productQuery.selectLoading();
    this.overlays$ = this.overlayQuery.selectAllAsUIOverlays();
    this.productTypeLoading$ = this.productTypeQuery.selectLoading();
    this.userLoggedIn$ = this.sessionQuery.isLoggedIn$();
    this.mapState$ = this.mapQuery.select();
    this.activeProductType$ = this.productTypeQuery.selectActive() as Observable<ProductType>;
    this.legend$ = this.legendQuery.selectLegend();
    this.legendState$ = this.legendQuery.select();

    this.productTypeService.get();
    this.overlayService.get();
  }

  selectProductType(productTypeId: number | null) {
    console.log('selectProductType', productTypeId);
    this.productTypeService.setActive(productTypeId);
  }

  selectProduct(productId: number) {
    this.productService.setActive(productId);
  }

  selectOverlay(overlayId: string) {

  }

  logout() {
    this.sessionService.logout();
  }

  toggleLegend() {
    this.legendService.toggleLegend();
  }
}
