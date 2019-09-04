import {Routes} from '@angular/router';
import {MapViewComponent} from './views/map-view/map-view.component';
import {ProfileComponent} from './views/profile/profile.component';
import {LoginComponent} from './views/login/login.component';
import {ResetPasswordComponent} from './views/reset-password/reset-password.component';
import {RegisterComponent} from './views/register/register.component';
import {IsLoggedIn, IsNotLoggedIn} from './utils/auth-guard/auth-guard.service';
import {ActivateComponent} from './views/activate/activate.component';
import {activateMatcher} from './utils';

export const appRoutes: Routes = [

  {
    path: '',
    component: MapViewComponent,
  }, {
    path: 'login',
    component: LoginComponent,
    canActivate: [IsNotLoggedIn]
  }, {
    path: 'register',
    component: RegisterComponent,
    canActivate: [IsNotLoggedIn]
  }, {
    path: 'profile',
    component: ProfileComponent,
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
