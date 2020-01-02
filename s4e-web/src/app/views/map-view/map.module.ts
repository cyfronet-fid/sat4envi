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
import {OwlDateTimeModule} from 'ng-pick-datetime';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {OwlMomentDateTimeModule} from 'ng-pick-datetime/date-time/adapter/moment-adapter/moment-date-time.module';
import {ProductQuery} from './state/product/product.query';
import {ProductService} from './state/product/product.service';
import {ProductStore} from './state/product/product.store';
import {SceneQuery} from './state/scene/scene.query.service';
import {SceneService} from './state/scene/scene.service';
import {SceneStore} from './state/scene/scene.store.service';
import {ReportModalComponent} from './zk/report-modal/report-modal.component';
import {makeModalProvider} from '../../modal/modal.providers';
import {REPORT_MODAL_ID} from './zk/report-modal/report-modal.model';
import {ModalModule} from '../../modal/modal.module';
import {SaveConfigModalComponent} from './zk/save-config-modal/save-config-modal.component';
import {SAVE_CONFIG_MODAL_ID} from './zk/save-config-modal/save-config-modal.model';
import {ListConfigsModalComponent} from './zk/list-configs-modal/list-configs-modal.component';
import {LIST_CONFIGS_MODAL_ID} from './zk/list-configs-modal/list-configs-modal.model';
import {RouterModule} from '@angular/router';
import {environment} from '../../../environments/environment';
import {IsLoggedIn} from '../../utils/auth-guard/auth-guard.service';
import {SentinelSearchComponent} from './sentinel-search/sentinel-search.component';
import {InjectorModule} from '../../common/injector.module';
import {ShareConfigurationModalComponent} from './zk/configuration/share-configuration-modal/share-configuration-modal.component';
import {SHARE_CONFIGURATION_MODAL_ID} from './zk/configuration/state/configuration.model';

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
    SentinelSearchComponent,
    ShareConfigurationModalComponent,
    SaveConfigModalComponent,
    ListConfigsModalComponent
  ],
  exports: [
    MapViewComponent,
  ],
  imports: [
    InjectorModule,
    ShareModule,
    BrowserAnimationsModule,
    OwlDateTimeModule,
    OwlMomentDateTimeModule,
    ModalModule,
    RouterModule.forChild([
      {
        path: 'map',
        component: MapViewComponent,
        canActivate: environment.inviteOnly ? [IsLoggedIn] : [],
        children: [
          {
            path: 'products',
            component: ViewManagerComponent
          },
          {
            path: 'sentinel-search',
            component: SentinelSearchComponent
          },
          {
            path: '',
            pathMatch: 'prefix',
            redirectTo: '/map/products'
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
    makeModalProvider(REPORT_MODAL_ID, ReportModalComponent),
    makeModalProvider(SHARE_CONFIGURATION_MODAL_ID, ShareConfigurationModalComponent),
    makeModalProvider(SAVE_CONFIG_MODAL_ID, SaveConfigModalComponent),
    makeModalProvider(LIST_CONFIGS_MODAL_ID, ListConfigsModalComponent)
  ],
  entryComponents: [
    ReportModalComponent,
    ShareConfigurationModalComponent,
    SaveConfigModalComponent,
    ListConfigsModalComponent
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
