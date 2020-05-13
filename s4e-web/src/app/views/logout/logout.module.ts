import {CommonStateModule} from '../../state/common-state.module';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LogoutComponent} from './logout.component';

@NgModule({
  declarations: [LogoutComponent],
  imports: [
    CommonModule,
    CommonStateModule
  ],
  exports: [LogoutComponent]
})
export class LogoutModule {
}
