import {ModuleWithProviders, NgModule} from '@angular/core';
import {MapViewComponent} from './map-view.component';
import {ViewManagerComponent} from './view-manager/view-manager.component';
import {CommonModule} from '../../common.module';
import {LoginModule} from '../../components/login/login.module';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {MapStore} from './state/map/map.store';
import { ProductPickerComponent } from './product-picker/product-picker.component';
import {ProductQuery} from './state/product/product.query';
import {ProductService} from './state/product/product.service';
import {ProductStore} from './state/product/product.store';
import {GranuleQuery} from './state/granule/granule.query';
import {GranuleService} from './state/granule/granule.service';
import {GranuleStore} from './state/granule/granule.store';
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
    ProductQuery,
    ProductService,
    ProductStore,
    GranuleQuery,
    GranuleService,
    GranuleStore,
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
        ProductQuery,
        ProductService,
        ProductStore,
        GranuleQuery,
        GranuleService,
        GranuleStore,
        OverlayQuery,
        OverlayService,
        OverlayStore
      ]
    };
  }
}
