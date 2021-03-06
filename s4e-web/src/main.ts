import '@angular/compiler';
import {enableProdMode} from '@angular/core';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {AppModule} from './app/app.module';
import {environment} from './environments/environment';
import {akitaConfig, enableAkitaProdMode, persistState} from '@datorama/akita';
import {hmrBootstrap} from './hmr';

akitaConfig({resettable: true});

if (environment.production) {
  enableProdMode();
  enableAkitaProdMode();
}

const bootstrap = () => platformBrowserDynamic().bootstrapModule(AppModule);

if (environment.hmr && environment.hmrPersist) {
  persistState({
    exclude: ['router'],
    key: 's4eStore'
  });

  if ((module as any).hot) {
    hmrBootstrap(module, bootstrap);
  } else {
    console.error(`
      HMR is not enabled for webpack-dev-server!/n
      Are you using the --hmr flag for ng serve?
    `);
  }
} else {
  bootstrap().catch(err => console.error(err));
}
