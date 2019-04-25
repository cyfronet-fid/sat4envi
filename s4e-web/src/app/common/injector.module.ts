import {Injector, NgModule} from '@angular/core';

@NgModule({
})
export class InjectorModule {
  static Injector: Injector;

  constructor(injector: Injector) {
    InjectorModule.Injector = injector;
  }
}
