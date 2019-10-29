import {Routes} from '@angular/router';
import {MapViewComponent} from './views/map-view/map-view.component';
import {LoginComponent} from './views/login/login.component';
import {ResetPasswordComponent} from './views/reset-password/reset-password.component';
import {RegisterComponent} from './views/register/register.component';
import {IsLoggedIn, IsNotLoggedIn} from './utils/auth-guard/auth-guard.service';
import {ActivateComponent} from './views/activate/activate.component';
import {activateMatcher} from './utils';
import {environment} from '../environments/environment';

export const appRoutes: Routes = [

  {
    path: '',
    component: MapViewComponent,
    canActivate: environment.inviteOnly ? [IsLoggedIn] : []
  }, {
    path: 'login',
    component: LoginComponent,
    canActivate: [IsNotLoggedIn]
  }, {
    path: 'register',
    component: RegisterComponent,
    canActivate: [IsNotLoggedIn]
  }, {
    path: 'reset-password',
    component: ResetPasswordComponent,
    canActivate: [IsNotLoggedIn]
  }, {
    matcher: activateMatcher,
    component: ActivateComponent
  }, {
    path: '**',
    redirectTo: '/',
    pathMatch: 'full',
  },
];
