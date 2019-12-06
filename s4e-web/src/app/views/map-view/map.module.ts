import {ModuleWithProviders, NgModule} from '@angular/core';
import {MapViewComponent} from './map-view.component';
import {ViewManagerComponent} from './view-manager/view-manager.component';
import {ShareModule} from '../../common/share.module';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {MapStore} from './state/map/map.store';
import {LayerPicker} from './view-manager/layer-picker/layer-picker.component';
import {TimelineComponent} from './timeline/timeline.component';
import {MapComponent} from './map/map.component';
import {OverlayQuery} from './state/overlay/overlay.query';
import {OverlayService} from './state/overlay/overlay.service';
import {OverlayStore} from './state/overlay/overlay.store';
import {LegendComponent} from './legend/legend.component';
import {LegendStore} from './state/legend/legend.store';
import {LegendQuery} from './state/legend/legend.query';
import {LegendService} from './state/legend/legend.service';
import {SearchResultsComponent} from './search-results/search-results.component';
import {AkitaGuidService} from './state/search-results/guid.service';
import {OwlDateTimeModule, OwlNativeDateTimeModule} from 'ng-pick-datetime';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {OwlMomentDateTimeModule} from 'ng-pick-datetime/date-time/adapter/moment-adapter/moment-date-time.module';
import {ProductQuery} from './state/product/product.query';
import {ProductService} from './state/product/product.service';
import {ProductStore} from './state/product/product.store';
import {SceneQuery} from './state/scene/scene.query.service';
import {SceneService} from './state/scene/scene.service';
import {SceneStore} from './state/scene/scene.store.service';
import {ReportModalComponent} from './report-modal/report-modal.component';
import {makeModalProvider} from '../../modal/modal.providers';
import {REPORT_MODAL_ID} from './report-modal/report-modal.model';
import {ModalModule} from '../../modal/modal.module';
import {RouterModule} from '@angular/router';
import {environment} from '../../../environments/environment';
import {IsLoggedIn} from '../../utils/auth-guard/auth-guard.service';
import { SentinelSearchComponent } from './sentinel-search/sentinel-search.component';

@NgModule({
  declarations: [
    MapViewComponent,
    ViewManagerComponent,
    LayerPicker,
    TimelineComponent,
    MapComponent,
    LegendComponent,
    SearchResultsComponent,
    ReportModalComponent,
    SentinelSearchComponent
  ],
  exports: [
    MapViewComponent,
  ],
  imports: [
    ShareModule,
    BrowserAnimationsModule,
    OwlDateTimeModule,
    OwlMomentDateTimeModule,
    ModalModule,
    RouterModule.forChild([
      {
        path: '',
        component: MapViewComponent,
        canActivate: environment.inviteOnly ? [IsLoggedIn] : [],
        children: [
          {
            path: 'map',
            component: ViewManagerComponent
          },
          {
            path: 'sentinel-search',
            component: SentinelSearchComponent
          },
        ]
      }
    ])
    // OwlNativeDateTimeModule
  ],
  providers: [
    MapQuery,
    MapService,
    MapStore,
    ProductQuery,
    ProductService,
    ProductStore,
    SceneQuery,
    SceneService,
    SceneStore,
    OverlayQuery,
    OverlayService,
    OverlayStore,
    LegendStore,
    LegendQuery,
    LegendService,
    AkitaGuidService,
    makeModalProvider(REPORT_MODAL_ID, ReportModalComponent)
  ],
  entryComponents: [
    ReportModalComponent
  ]
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
        SceneQuery,
        SceneService,
        SceneStore,
        OverlayQuery,
        OverlayService,
        OverlayStore,
        LegendQuery,
        LegendService,
        AkitaGuidService
      ]
    };
  }
}
