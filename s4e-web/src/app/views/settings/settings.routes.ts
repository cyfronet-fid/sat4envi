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

import {InstitutionFormComponent} from './manage-institutions/institution-form/institution-form.component';
import {ChangePasswordComponent} from './profile/change-password/change-password.component';
import {Routes} from '@angular/router';
import {ProfileComponent} from './profile/profile.component';
import {SettingsComponent} from './settings.component';
import {IsLoggedIn} from '../../utils/auth-guard/auth-guard.service';
import {DashboardComponent} from './dashboard/dashboard.component';
import {PersonListComponent} from './people/person-list/person-list.component';
import {IsManagerGuard} from './guards/is-manager/is-manager.guard';
import {InstitutionListComponent} from './manage-institutions/institution-list/institution-list.component';
import {InstitutionProfileComponent} from './intitution-profile/institution-profile.component';
import {
  ADD_INSTITUTION_PATH,
  EDIT_INSTITUTION_PATH,
  INSTITUTION_PROFILE_PATH,
  INSTITUTIONS_LIST_PATH,
  SETTINGS_PATH
} from './settings.breadcrumbs';
import {ManageAuthoritiesComponent} from './manage-authorities/manage-authorities.component';
import {IsAdminGuard} from './guards/is-admin.guard';
import {
  GLOBAL_OVERLAYS_PATH,
  INSTITUTION_OVERLAYS_PATH,
  WmsOverlaysComponent
} from './wms-overlays/wms-overlays.component';
import {ManageProductsComponent} from './manage-products/manage-products.component';

export const settingsRoutes: Routes = [
  {
    path: SETTINGS_PATH,
    component: SettingsComponent,
    canActivate: [IsLoggedIn],
    children: [
      {
        path: 'dashboard',
        component: DashboardComponent
      },
      {
        path: 'manage-authorities',
        component: ManageAuthoritiesComponent
      },
      {
        path: INSTITUTIONS_LIST_PATH,
        component: InstitutionListComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: INSTITUTION_PROFILE_PATH,
        component: InstitutionProfileComponent,
        canActivate: [IsLoggedIn]
      },
      {
        path: 'manage-products',
        component: ManageProductsComponent
      },
      {
        path: ADD_INSTITUTION_PATH,
        component: InstitutionFormComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: INSTITUTION_OVERLAYS_PATH,
        component: WmsOverlaysComponent
      },
      {
        path: GLOBAL_OVERLAYS_PATH,
        component: WmsOverlaysComponent,
        canActivate: [IsAdminGuard]
      },
      {
        path: EDIT_INSTITUTION_PATH,
        component: InstitutionFormComponent,
        canActivate: [IsManagerGuard]
      },

      {
        path: 'people',
        component: PersonListComponent,
        canActivate: [IsManagerGuard]
      },

      {
        path: 'profile',
        component: ProfileComponent
      },
      {
        path: 'change-password',
        component: ChangePasswordComponent
      },

      {
        path: '**',
        redirectTo: 'profile',
        pathMatch: 'full'
      }
    ]
  }
];
