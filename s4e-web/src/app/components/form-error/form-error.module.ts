import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ShareModule} from '../../common/share.module';
import {FormErrorComponent} from './form-error.component';

@NgModule({
  declarations: [
    FormErrorComponent
  ],
  imports: [
    CommonModule,
    ShareModule
  ],
  exports: [
    FormErrorComponent
  ]
})
export class FormErrorModule { }
