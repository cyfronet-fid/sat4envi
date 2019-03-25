import {ModuleWithProviders, NgModule} from '@angular/core';
import {MapViewComponent} from './map-view.component';
import {ViewManagerComponent} from './view-manager/view-manager.component';
import {CommonModule} from '../../common.module';
import {LoginModule} from '../../components/login/login.module';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {MapStore} from './state/map/map.store';
import { ProductPickerComponent } from './product-picker/product-picker.component';
import {ProductTypeQuery} from './state/product-type/product-type-query.service';
import {ProductTypeService} from './state/product-type/product-type.service';
import {ProductTypeStore} from './state/product-type/product-type-store.service';
import {ProductQuery} from './state/product/product-query.service';
import {ProductService} from './state/product/product.service';
import {ProductStore} from './state/product/product-store.service';
import {ConstantsProvider} from '../../app.constants';
import { TimelineComponent } from './timeline/timeline.component';
import { MapComponent } from './map/map.component';
import {OverlayQuery} from './state/overlay/overlay.query';
import {OverlayService} from './state/overlay/overlay.service';
import {OverlayStore} from './state/overlay/overlay.store';
import {BsDropdownModule} from 'ngx-bootstrap';

@NgModule({
  declarations: [
    MapViewComponent,
    ViewManagerComponent,
    ProductPickerComponent,
    TimelineComponent,
    MapComponent,
  ],
  exports: [
    MapViewComponent,
  ],
  imports: [
    CommonModule,
    LoginModule,
    BsDropdownModule
  ],
  providers: [
    MapQuery,
    MapService,
    MapStore,
    ProductTypeQuery,
    ProductTypeService,
    ProductTypeStore,
    ProductQuery,
    ProductService,
    ProductStore,
    ConstantsProvider,
    OverlayQuery,
    OverlayService,
    OverlayStore
  ],
})
export class MapModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: MapModule,
      providers: [
        MapQuery,
        MapService,
        MapStore,
        ProductTypeQuery,
        ProductTypeService,
        ProductTypeStore,
        ProductQuery,
        ProductService,
        ProductStore,
        OverlayQuery,
        OverlayService,
        OverlayStore
      ]
    };
  }
}
