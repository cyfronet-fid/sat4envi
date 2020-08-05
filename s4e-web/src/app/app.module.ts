import { ConfigurationLoader } from './utils/initializer/config.service';
import {LogoutModule} from './views/logout/logout.module';
import {APP_INITIALIZER, LOCALE_ID, NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {RouterModule} from '@angular/router';
import {ShareModule} from './common/share.module';
import {MapModule} from './views/map-view/map.module';
import {ProfileModule} from './views/settings/profile/profile.module';
import {RootComponent} from './components/root/root.component';
import {ErrorInterceptor} from './utils/error-interceptor/error.interceptor';
import {appRoutes} from './app.routes';
import {environment} from '../environments/environment';
import {akitaConfig} from '@datorama/akita';
import {CommonStateModule} from './state/common-state.module';
import {AkitaNgDevtools} from '@datorama/akita-ngdevtools';
import {registerLocaleData} from '@angular/common';
import localePl from '@angular/common/locales/pl';
import {LoginComponent} from './views/login/login.component';
import {LoginModule} from './views/login/login.module';
import {RegisterModule} from './views/register/register.module';
import {ResetPasswordModule} from './views/reset-password/reset-password.module';
import {ActivateModule} from './views/activate/activate.module';
import {InjectorModule} from './common/injector.module';
import {SettingsModule} from './views/settings/settings.module';
import {ModalModule} from './modal/modal.module';
import {S4EFormsModule} from './form/form.module';
import {NotificationsModule} from 'notifications';
import {ErrorsModule} from './errors/errors.module';
import {NgxUiLoaderConfig, NgxUiLoaderModule, PB_DIRECTION, POSITION, SPINNER} from 'ngx-ui-loader';
import {AkitaNgRouterStoreModule} from '@datorama/akita-ng-router-store';

registerLocaleData(localePl, 'pl');


export function initializeConfiguration(loader: ConfigurationLoader): () => Promise<any> {
  return () => loader.load$();
}

const ngxUiLoaderConfig: NgxUiLoaderConfig = {
  bgsColor: 'white',
  pbColor: 'white',
  bgsOpacity: 1,
  bgsPosition: POSITION.centerCenter,
  bgsSize: 120,
  bgsType: SPINNER.threeBounce,
  pbDirection: PB_DIRECTION.leftToRight,
  pbThickness: 5,
  hasProgressBar: true,
  fgsSize: 80
};

@NgModule({
  declarations: [
    RootComponent
  ],
  imports: [
    AkitaNgRouterStoreModule,
    ...(environment.production ? [] : [AkitaNgDevtools.forRoot()]),
    RouterModule.forRoot(appRoutes, {enableTracing: false}),
    ...ShareModule.modulesForRoot(),
    LoginModule,
    LogoutModule,
    RegisterModule,
    ActivateModule,
    ResetPasswordModule,
    CommonStateModule,
    MapModule,
    ProfileModule,
    InjectorModule,
    SettingsModule,
    ModalModule,
    S4EFormsModule,
    NotificationsModule.forRoot(environment.production),
    ErrorsModule,
    NgxUiLoaderModule.forRoot(ngxUiLoaderConfig)
  ],
  providers: [
    ConfigurationLoader,
    {provide: APP_INITIALIZER, useFactory: initializeConfiguration, deps: [ConfigurationLoader], multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    {provide: LOCALE_ID, useValue: 'pl-PL'}
  ],
  bootstrap: [RootComponent],
  exports: [LoginComponent],
})
export class AppModule {
  constructor() {
    akitaConfig({resettable: true});
  }
}
