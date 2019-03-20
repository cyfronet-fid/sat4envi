import {ModuleWithProviders, NgModule} from '@angular/core';
import { CommonModule } from '@angular/common';
import {SessionQuery} from './session/session.query';
import {SessionService} from './session/session.service';

@NgModule({
  declarations: [],
  imports: [
    CommonModule
  ]
})
export class CommonStateModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: CommonStateModule,
      providers: [
        SessionQuery,
        SessionService,
      ]
    };
  }
}
