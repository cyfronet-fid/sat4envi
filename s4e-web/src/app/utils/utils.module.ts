import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ErrorKeysPipe} from './error-keys/error-keys.pipe';

@NgModule({
  declarations: [
    ErrorKeysPipe
  ],
  imports: [
    CommonModule
  ], exports: [
    ErrorKeysPipe
  ]
})
export class UtilsModule {
}
