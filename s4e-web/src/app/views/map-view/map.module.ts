import { FormErrorModule } from 'src/app/components/form-error/form-error.module';
import {NgModule} from '@angular/core';
import {SearchModule} from '../../components/search/search.module';
import {MapViewComponent} from './map-view.component';
import {ViewManagerComponent} from './view-manager/view-manager.component';
import {ShareModule} from '../../common/share.module';
import {MapQuery} from './state/map/map.query';
import {MapService} from './state/map/map.service';
import {MapStore} from './state/map/map.store';
import {ItemsPickerComponent} from './view-manager/layer-picker/layer-picker.component';
import {TimelineComponent} from './timeline/timeline.component';
import {MapComponent} from './map/map.component';
import {OverlayQuery} from './state/overlay/overlay.query';
import {OverlayService} from './state/overlay/overlay.service';
import {OverlayStore} from './state/overlay/overlay.store';
import {LegendComponent} from './legend/legend.component';
import {LegendStore} from './state/legend/legend.store';
import {LegendQuery} from './state/legend/legend.query';
import {LegendService} from './state/legend/legend.service';
import {AkitaGuidService} from './state/search-results/guid.service';
import {OwlDateTimeModule, OwlNativeDateTimeModule} from 'ng-pick-datetime';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
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
import {SentinelSearchComponent} from './sentinel-search/sentinel-search.component';
import {InjectorModule} from '../../common/injector.module';
import {ShareConfigurationModalComponent} from './zk/configuration/share-configuration-modal/share-configuration-modal.component';
import {SHARE_CONFIGURATION_MODAL_ID} from './zk/configuration/state/configuration.model';
import {S4EFormsModule} from '../../form/form.module';
import {DynamicSpaceDirective} from './view-manager/dynamic-space.directive';
import {SentinelFormComponent} from './sentinel-search/sentinel-form/sentinel-form.component';
import {SentinelSectionComponent} from './sentinel-search/sentinel-section/sentinel-section.component';
import {FormsModule} from '@angular/forms';
import {AkitaNgRouterStoreModule} from '@datorama/akita-ng-router-store';
import {routes} from './map.routes.module';
import {SearchResultModalComponent} from './sentinel-search/search-result-modal/search-result-modal.component';
import {SENTINEL_SEARCH_RESULT_MODAL_ID} from './sentinel-search/search-result-modal/search-result-modal.model';
import { ResizableModule } from 'angular-resizable-element';
import { EventsModule } from 'src/app/utils/search/events.module';

@NgModule({
  declarations: [
    MapViewComponent,
    ViewManagerComponent,
    ItemsPickerComponent,
    TimelineComponent,
    MapComponent,
    LegendComponent,
    ReportModalComponent,
    SentinelSearchComponent,
    ShareConfigurationModalComponent,
    SaveConfigModalComponent,
    ListConfigsModalComponent,
    DynamicSpaceDirective,
    SentinelFormComponent,
    SentinelSectionComponent,
    SearchResultModalComponent
  ],
  exports: [
    MapViewComponent,
  ],
  imports: [
    InjectorModule,
    ShareModule,
    FormsModule,
    BrowserAnimationsModule,
    AkitaNgRouterStoreModule,
    OwlDateTimeModule,
    OwlNativeDateTimeModule,
    ModalModule,
    RouterModule.forChild(routes),
    S4EFormsModule,
    SearchModule,
    FormErrorModule,
    ResizableModule,
    EventsModule
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
    makeModalProvider(LIST_CONFIGS_MODAL_ID, ListConfigsModalComponent),
    makeModalProvider(SENTINEL_SEARCH_RESULT_MODAL_ID, SearchResultModalComponent)
  ],
  entryComponents: [
    ReportModalComponent,
    ShareConfigurationModalComponent,
    SaveConfigModalComponent,
    ListConfigsModalComponent,
    SearchResultModalComponent
  ]
})
export class MapModule {
}
