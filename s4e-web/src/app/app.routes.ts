import {Routes} from '@angular/router';
import {LoginComponent} from './views/login/login.component';
import {ResetPasswordComponent} from './views/reset-password/reset-password.component';
import {RegisterComponent} from './views/register/register.component';
import {IsNotLoggedIn} from './utils/auth-guard/auth-guard.service';
import {ActivateComponent} from './views/activate/activate.component';
import {activateMatcher} from './utils';

export const appRoutes: Routes = [
  {
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
    redirectTo: '/map',
    pathMatch: 'full',
  },
];
