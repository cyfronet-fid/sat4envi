import {ModuleWithProviders, NgModule} from '@angular/core';
import { CommonModule } from '@angular/common';
import {SessionQuery} from './session/session.query';
import {SessionService} from './session/session.service';

@NgModule({
  declarations: [],
  imports: [
    CommonModule
  ],
  providers: [
    SessionQuery,
    SessionService,
  ]
})
export class CommonStateModule {}
