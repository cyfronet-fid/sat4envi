import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ErrorKeysPipe} from './error-keys/error-keys.pipe';
import {S4EDatePipe} from './s4e-date/s4e-date.pipe';
import {GroupArrayPipe} from './group-array/group-array.pipe';
import {IsEmptyPipe} from './is-empty/is-empty.pipe';
import {HourPipe} from './time/hour.pipe';

@NgModule({
  declarations: [
    ErrorKeysPipe,
    S4EDatePipe,
    GroupArrayPipe,
    IsEmptyPipe,
    HourPipe
  ],
  imports: [
    CommonModule
  ],
  exports: [
    ErrorKeysPipe,
    GroupArrayPipe,
    S4EDatePipe,
    IsEmptyPipe,
    HourPipe
  ]
})
export class UtilsModule {
}
