/*
 * Copyright 2020 ACC Cyfronet AGH
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

import { RegisterConfirmationComponent } from './views/register/register-confirmation/register-confirmation.component';
import {LogoutComponent} from './views/logout/logout.component';
import {Routes} from '@angular/router';
import {LoginComponent} from './views/login/login.component';
import {ResetPasswordComponent} from './views/reset-password/reset-password.component';
import {RegisterComponent} from './views/register/register.component';
import {IsLoggedIn, IsNotLoggedIn} from './utils/auth-guard/auth-guard.service';
import {ActivateComponent} from './views/activate/activate.component';
import {ApihowtoComponent} from './views/apihowto/apihowto.component';
import {activateMatcher} from './utils';
import {HTTP_404_NOT_FOUND} from './errors/errors.model';

export const appRoutes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [IsNotLoggedIn]
  }, {
    path: 'logout',
    component: LogoutComponent,
    canActivate: [IsLoggedIn]
  }, {
    path: 'register',
    component: RegisterComponent,
    canActivate: [IsNotLoggedIn]
  }, {
    path: 'register-confirmation',
    component: RegisterConfirmationComponent,
    canActivate: [IsNotLoggedIn]
  }, {
    path: 'reset-password',
    component: ResetPasswordComponent,
    canActivate: [IsNotLoggedIn]
  }, {
    path: 'howto',
    component: ApihowtoComponent
  },

  {
    matcher: activateMatcher,
    component: ActivateComponent
  }, {
    path: '',
    redirectTo: '/map/products',
    pathMatch: 'full'
  }, {
    path: '**',
    redirectTo: `errors/${HTTP_404_NOT_FOUND}`,
    pathMatch: 'full',
  }
];
