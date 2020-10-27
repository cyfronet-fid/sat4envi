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
