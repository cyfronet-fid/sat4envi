import {ModuleWithProviders, NgModule} from '@angular/core';
import {MapViewComponent} from './map-view.component';
import {ViewManagerComponent} from './view-manager/view-manager.component';
import {ShareModule} from '../../common/share.module';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {MapStore} from './state/map/map.store';
import { LayerPicker } from './view-manager/layer-picker/layer-picker.component';
import {ProductTypeQuery} from './state/product-type/product-type.query';
import {ProductTypeService} from './state/product-type/product-type.service';
import {ProductTypeStore} from './state/product-type/product-type.store';
import {ProductQuery} from './state/product/product.query';
import {ProductService} from './state/product/product.service';
import {ProductStore} from './state/product/product.store';
import { TimelineComponent } from './timeline/timeline.component';
import { MapComponent } from './map/map.component';
import {OverlayQuery} from './state/overlay/overlay.query';
import {OverlayService} from './state/overlay/overlay.service';
import {OverlayStore} from './state/overlay/overlay.store';
import {BsDropdownModule} from 'ngx-bootstrap';
import { LegendComponent } from './legend/legend.component';
import {LegendStore} from './state/legend/legend.store';
import {LegendQuery} from './state/legend/legend.query';
import {LegendService} from './state/legend/legend.service';

@NgModule({
  declarations: [
    MapViewComponent,
    ViewManagerComponent,
    LayerPicker,
    TimelineComponent,
    MapComponent,
    LegendComponent,
  ],
  exports: [
    MapViewComponent,
  ],
  imports: [
    ShareModule,
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
    OverlayQuery,
    OverlayService,
    OverlayStore,
    LegendStore,
    LegendQuery,
    LegendService
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
        OverlayStore,
        LegendQuery,
        LegendService
      ]
    };
  }
}
