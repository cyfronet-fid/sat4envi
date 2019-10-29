import {APP_INITIALIZER, LOCALE_ID, NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {RouterModule} from '@angular/router';
import {ShareModule} from './common/share.module';
import {MapModule} from './views/map-view/map.module';
import {ProfileModule} from './views/settings/profile/profile.module';
import {RootComponent} from './components/root/root.component';
import {ContentTypeInterceptor} from './utils/content-type-interceptor/content-type.interceptor';
import {AuthInterceptor} from './utils/auth-interceptor/auth.interceptor';
import {ErrorInterceptor} from './utils/error-interceptor/error.interceptor';
import {appRoutes} from './app.routes';
import {environment} from '../environments/environment';
import {akitaConfig} from '@datorama/akita';
import {SessionQuery} from './state/session/session.query';
import {SessionService} from './state/session/session.service';
import {CommonStateModule} from './state/common-state.module';
import {AkitaNgRouterStoreModule} from '@datorama/akita-ng-router-store';
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

registerLocaleData(localePl, 'pl');


export function initializeApp(configService: S4eConfig): () => Promise<any> {
  return () => configService.loadConfiguration();
}

@NgModule({
  declarations: [
    RootComponent
  ],
  imports: [
    ...(environment.production ? [] : [AkitaNgDevtools.forRoot(), AkitaNgRouterStoreModule.forRoot()]),
    ...ShareModule.modulesForRoot(),
    LoginModule,
    RegisterModule,
    ActivateModule,
    ResetPasswordModule,
    RouterModule.forRoot(appRoutes),
    CommonStateModule.forRoot(),
    MapModule,
    ProfileModule,
    InjectorModule,
    SettingsModule
  ],
  providers: [
    S4eConfig,
    {provide: APP_INITIALIZER, useFactory: initializeApp, deps: [S4eConfig], multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ContentTypeInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    {provide: LOCALE_ID, useValue: 'pl-PL'}
  ],
  bootstrap: [RootComponent],
  exports: [LoginComponent],
})
export class AppModule {
  constructor(private sessionService: SessionService, private sessionQuery: SessionQuery) {
    akitaConfig({
      resettable: true
    });

    if (environment.hmr === false && !sessionQuery.isInitialized()) {
      this.sessionService.init();
    }

    // this.bootstrapService.init();
  }
}
