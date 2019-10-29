import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ErrorKeysPipe} from './error-keys/error-keys.pipe';
import { S4EDatePipe } from './s4e-date/s4e-date.pipe';

@NgModule({
  declarations: [
    ErrorKeysPipe,
    S4EDatePipe
  ],
  imports: [
    CommonModule
  ], exports: [
    ErrorKeysPipe
  ]
})
export class UtilsModule {
}
