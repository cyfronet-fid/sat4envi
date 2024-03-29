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

import {ConfigurationLoader} from './utils/initializer/config.service';
import {LogoutModule} from './views/logout/logout.module';
import {APP_INITIALIZER, LOCALE_ID, NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {NavigationEnd, Router, RouterModule} from '@angular/router';
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
import {ApihowtoModule} from './views/apihowto/apihowto.module';
import {LoginModule} from './views/login/login.module';
import {RegisterModule} from './views/register/register.module';
import {ResetPasswordModule} from './views/reset-password/reset-password.module';
import {ActivateModule} from './views/activate/activate.module';
import {InjectorModule} from './common/injector.module';
import {SettingsModule} from './views/settings/settings.module';
import {ModalModule} from './modal/modal.module';
import {S4EFormsModule} from './form/form.module';
import {ErrorsModule} from './errors/errors.module';

import {
  AkitaNgRouterStoreModule,
  RouterQuery
} from '@datorama/akita-ng-router-store';
import {LocalStorage, LOCATION, WINDOW} from './app.providers';
import {ProfileLoaderService} from './state/session/session.service';
import {NotificationsModule} from './notifications/notifications.module';
import {BsDatepickerModule, BsLocaleService} from 'ngx-bootstrap/datepicker';
import {defineLocale} from 'ngx-bootstrap/chronos';
import {plLocale} from 'ngx-bootstrap/locale';
import {AuxiliaryServicesLoader} from './utils/auxiliary/auxiliary-services-loader.service';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {filter} from 'rxjs/operators';
import {NgxUiLoaderModule, PB_DIRECTION, POSITION, SPINNER} from 'ngx-ui-loader';

defineLocale('pl', plLocale);
registerLocaleData(localePl, 'pl');

export function initializeConfiguration(
  loader: ConfigurationLoader
): () => Promise<any> {
  return () => loader.load$();
}

export function initializeHelpDesk(
  loader: AuxiliaryServicesLoader
): () => Promise<boolean> {
  return () => loader.load$();
}

export function initializeProfile(
  profileLoaderService: ProfileLoaderService
): () => Promise<any> {
  return () => profileLoaderService.loadProfile$().toPromise();
}

@UntilDestroy()
@NgModule({
  declarations: [RootComponent],
  imports: [
    AkitaNgRouterStoreModule,
    ...(environment.production ? [] : [AkitaNgDevtools.forRoot()]),
    RouterModule.forRoot(appRoutes, {
      enableTracing: false,
      relativeLinkResolution: 'legacy'
    }),
    ...ShareModule.modulesForRoot(),
    BsDatepickerModule.forRoot(),
    LoginModule,
    LogoutModule,
    RegisterModule,
    ApihowtoModule,
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
    NgxUiLoaderModule.forRoot({
      bgsOpacity: 1,
      bgsPosition: POSITION.topCenter
    })
  ],
  providers: [
    ConfigurationLoader,
    {
      provide: APP_INITIALIZER,
      useFactory: initializeConfiguration,
      deps: [ConfigurationLoader],
      multi: true
    },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeHelpDesk,
      deps: [AuxiliaryServicesLoader],
      multi: true
    },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeProfile,
      deps: [ProfileLoaderService],
      multi: true
    },
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    {provide: LOCALE_ID, useValue: 'pl-PL'},
    {provide: LocalStorage, useValue: window.localStorage},
    {provide: LOCATION, useValue: window.location},
    {provide: WINDOW, useValue: window}
  ],
  bootstrap: [RootComponent],
  exports: [LoginComponent]
})
export class AppModule {
  constructor(
    locale: BsLocaleService,
    router: Router,
    auxiliaryServices: AuxiliaryServicesLoader
  ) {
    locale.use('pl');
    akitaConfig({resettable: true});

    router.events
      .pipe(
        untilDestroyed(this),
        filter(event => event instanceof NavigationEnd)
      )
      .subscribe(() => auxiliaryServices.updateAnalytics());
  }
}
