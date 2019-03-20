import {LOCALE_ID, NgModule} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClient} from '@angular/common/http';
import { RouterModule } from '@angular/router';
import {CommonModule} from './common.module';
import {MapModule} from './views/map-view/map.module';
import {ProfileModule} from './views/profile/profile.module';
import {RootComponent} from './components/root/root.component';
import {ContentTypeInterceptor} from './utils/content-type-interceptor/content-type.interceptor';
import {AuthInterceptor} from './utils/auth-interceptor/auth.interceptor';
import {ErrorInterceptor} from './utils/error-interceptor/error.interceptor';
import { appRoutes } from './app.routes';
import {environment} from '../environments/environment';
import {akitaConfig} from '@datorama/akita';
import {SessionQuery} from './state/session/session.query';
import {SessionService} from './state/session/session.service';
import {CommonStateModule} from './state/common-state.module';
import {ConstantsProvider} from './app.constants';
import {AkitaNgRouterStoreModule} from '@datorama/akita-ng-router-store';
import {AkitaNgDevtools} from '@datorama/akita-ngdevtools';
import {registerLocaleData} from '@angular/common';
import localePl from '@angular/common/locales/pl';

registerLocaleData(localePl, 'pl');

@NgModule({
  declarations: [
    RootComponent,
  ],
  imports: [
    ...(environment.production ? [] : [AkitaNgDevtools.forRoot(), AkitaNgRouterStoreModule.forRoot()]),
    ...CommonModule.modulesForRoot(),
    RouterModule.forRoot(appRoutes),
    CommonStateModule.forRoot(),
    MapModule,
    ProfileModule,
  ],
  providers: [
    ConstantsProvider,
    { provide: HTTP_INTERCEPTORS, useClass: ContentTypeInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    {provide: LOCALE_ID, useValue: 'pl-PL' }
  ],
  bootstrap: [RootComponent],
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
