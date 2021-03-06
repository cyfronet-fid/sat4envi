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

import {ExpertHelpModalComponent} from './zk/expert-help-modal/expert-help-modal.component';
import {EXPERT_HELP_MODAL_ID} from './zk/expert-help-modal/expert-help-modal.model';
import {JWT_TOKEN_MODAL_ID} from './jwt-token-modal/jwt-token-modal.model';
import {JwtTokenModalComponent} from './jwt-token-modal/jwt-token-modal.component';
import {FormErrorModule} from 'src/app/components/form-error/form-error.module';
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
import {BsDatepickerModule} from 'ngx-bootstrap/datepicker';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ProductQuery} from './state/product/product.query';
import {ProductService} from './state/product/product.service';
import {ProductStore} from './state/product/product.store';
import {SceneQuery} from './state/scene/scene.query';
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
import {ResizableModule} from 'angular-resizable-element';
import {EventsModule} from 'src/app/utils/search/events.module';
import {OVERLAY_LIST_MODAL_ID} from './view-manager/overlay-list-modal/overlay-list-modal.model';
import {OverlayListModalComponent} from './view-manager/overlay-list-modal/overlay-list-modal.component';
import {
  SearchResultsComponent,
  ToPaginationArrayPipe
} from './sentinel-search/search-results/search-results.component';
import {ReportTemplatesModalComponent} from './zk/report-templates-modal/report-templates-modal.component';
import {REPORT_TEMPLATES_MODAL_ID} from './zk/report-templates-modal/report-templates-modal.model';
import {LegendDesignerComponent} from './legend/legend-designer/legend-designer.component';
import {OverlayListModule} from '../../components/overlay-list/overlay-list.module';
import {UserDropdownComponent} from './user-dropdown/user-dropdown.component';
import {MobileSceneSelectorModalComponent} from './timeline/mobile-scene-selector-modal/mobile-scene-selector-modal.component';
import {MOBILE_MODAL_SCENE_SELECTOR_MODAL_ID} from './timeline/mobile-scene-selector-modal/mobile-scene-selector-modal.model';

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
    SearchResultsComponent,
    SearchResultModalComponent,
    OverlayListModalComponent,
    SearchResultsComponent,
    JwtTokenModalComponent,
    ExpertHelpModalComponent,
    LegendDesignerComponent,
    ExpertHelpModalComponent,
    ReportTemplatesModalComponent,
    ToPaginationArrayPipe,
    UserDropdownComponent,
    MobileSceneSelectorModalComponent
  ],
  exports: [MapViewComponent],
  imports: [
    BrowserAnimationsModule,
    ShareModule,
    FormsModule,
    BrowserAnimationsModule,
    AkitaNgRouterStoreModule,
    BsDatepickerModule,
    ModalModule,
    RouterModule.forChild(routes),
    S4EFormsModule,
    SearchModule,
    FormErrorModule,
    ResizableModule,
    EventsModule,
    OverlayListModule
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
    makeModalProvider(
      SHARE_CONFIGURATION_MODAL_ID,
      ShareConfigurationModalComponent
    ),
    makeModalProvider(SAVE_CONFIG_MODAL_ID, SaveConfigModalComponent),
    makeModalProvider(LIST_CONFIGS_MODAL_ID, ListConfigsModalComponent),
    makeModalProvider(SENTINEL_SEARCH_RESULT_MODAL_ID, SearchResultModalComponent),
    makeModalProvider(OVERLAY_LIST_MODAL_ID, OverlayListModalComponent),
    makeModalProvider(JWT_TOKEN_MODAL_ID, JwtTokenModalComponent),
    makeModalProvider(EXPERT_HELP_MODAL_ID, ExpertHelpModalComponent),
    makeModalProvider(REPORT_TEMPLATES_MODAL_ID, ReportTemplatesModalComponent),
    makeModalProvider(
      MOBILE_MODAL_SCENE_SELECTOR_MODAL_ID,
      MobileSceneSelectorModalComponent
    )
  ],
  entryComponents: [
    ReportModalComponent,
    ShareConfigurationModalComponent,
    SaveConfigModalComponent,
    ListConfigsModalComponent,
    SearchResultModalComponent,
    OverlayListModalComponent,
    JwtTokenModalComponent,
    ExpertHelpModalComponent,
    ReportTemplatesModalComponent,
    MobileSceneSelectorModalComponent
  ]
})
export class MapModule {}
