import {LogoutModule} from './views/logout/logout.module';
import {APP_INITIALIZER, LOCALE_ID, NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {RouterModule} from '@angular/router';
import {ShareModule} from './common/share.module';
import {MapModule} from './views/map-view/map.module';
import {ProfileModule} from './views/settings/profile/profile.module';
import {RootComponent} from './components/root/root.component';
import {AuthInterceptor} from './utils/auth-interceptor/auth.interceptor';
import {ErrorInterceptor} from './utils/error-interceptor/error.interceptor';
import {appRoutes} from './app.routes';
import {environment} from '../environments/environment';
import {akitaConfig} from '@datorama/akita';
import {SessionQuery} from './state/session/session.query';
import {SessionService} from './state/session/session.service';
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
import {S4eConfig} from './utils/initializer/config.service';
import {SettingsModule} from './views/settings/settings.module';
import {ModalModule} from './modal/modal.module';
import {S4EFormsModule} from './form/form.module';
import {NotificationsModule} from 'notifications';
import {ErrorsModule} from './errors/errors.module';
import {NgxUiLoaderConfig, NgxUiLoaderModule, PB_DIRECTION, POSITION, SPINNER} from 'ngx-ui-loader';
import {AkitaNgRouterStoreModule} from '@datorama/akita-ng-router-store';

registerLocaleData(localePl, 'pl');


export function initializeApp(configService: S4eConfig): () => Promise<any> {
  return () => configService.loadConfiguration();
}

const ngxUiLoaderConfig: NgxUiLoaderConfig = {
  bgsColor: 'red',
  pbColor: 'red',
  bgsOpacity: 1,
  bgsPosition: POSITION.centerCenter,
  bgsSize: 120,
  bgsType: SPINNER.ballSpinClockwise,
  pbDirection: PB_DIRECTION.leftToRight,
  pbThickness: 5,
  hasProgressBar: true
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
    S4eConfig,
    {provide: APP_INITIALIZER, useFactory: initializeApp, deps: [S4eConfig], multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    {provide: LOCALE_ID, useValue: 'pl-PL'}
  ],
  bootstrap: [RootComponent],
  exports: [LoginComponent],
})
export class AppModule {
  constructor(
    private sessionService: SessionService,
    private sessionQuery: SessionQuery
  ) {
    akitaConfig({
      resettable: true
    });

    if (environment.hmr === false && !this.sessionQuery.isInitialized()) {
      this.sessionService.init();
    }
  }
}
