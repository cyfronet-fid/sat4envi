import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ErrorKeysPipe} from './error-keys/error-keys.pipe';
import { S4EDatePipe } from './s4e-date/s4e-date.pipe';
import { GroupArrayPipe } from './group-array/group-array.pipe';

@NgModule({
  declarations: [
    ErrorKeysPipe,
    S4EDatePipe,
    GroupArrayPipe
  ],
  imports: [
    CommonModule
  ],
  exports: [
    ErrorKeysPipe,
    GroupArrayPipe
  ]
})
export class UtilsModule {
}
