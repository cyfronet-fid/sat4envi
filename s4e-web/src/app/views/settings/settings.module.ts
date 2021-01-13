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

import {ModalModule} from '../../modal/modal.module';
import {SearchModule} from '../../components/search/search.module';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {settingsRoutes} from './settings.routes';
import {SettingsComponent} from './settings.component';
import {ProfileModule} from './profile/profile.module';
import {InstitutionProfileModule} from './intitution-profile/institution-profile.module';
import {DashboardModule} from './dashboard/dashboard.module';
import {PeopleModule} from './people/people.module';
import {ReactiveFormsModule} from '@angular/forms';
import {S4EFormsModule} from '../../form/form.module';
import {ManageInstitutionsModule} from './manage-institutions/manage-institutions.module';
import {SETTINGS_PATH} from './settings.breadcrumbs';
import { EventsModule } from 'src/app/utils/search/events.module';
import {ManageAuthoritiesComponent} from './manage-authorities/manage-authorities.component';
import {ManageProductsComponent} from './manage-products/manage-products.component';
import {GenericListViewModule} from './components/generic-list-view/generic-list-view.module';
import {OverlayListModule} from '../../components/overlay-list/overlay-list.module';
import {WmsOverlaysComponent} from './wms-overlays/wms-overlays.component';

@NgModule({
  declarations: [
    SettingsComponent,
    ManageAuthoritiesComponent,
    WmsOverlaysComponent,
    ManageProductsComponent
  ],
  imports: [
    CommonModule,
    S4EFormsModule,
    ReactiveFormsModule,
    ProfileModule,
    InstitutionProfileModule,
    DashboardModule,
    PeopleModule,
    RouterModule.forChild(settingsRoutes),
    ManageInstitutionsModule,
    SearchModule,
    ModalModule,
    EventsModule,
    GenericListViewModule,
    OverlayListModule
  ]
})
export class SettingsModule {}
