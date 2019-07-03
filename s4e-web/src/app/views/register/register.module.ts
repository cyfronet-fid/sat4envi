import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RegisterComponent} from './register.component';
import {ReactiveFormsModule} from '@angular/forms';
import {ShareModule} from '../../common/share.module';
import {UtilsModule} from '../../utils/utils.module';
import {FormErrorModule} from '../../components/form-error/form-error.module';

@NgModule({
  declarations: [
    RegisterComponent,
  ],
  imports: [
    CommonModule,
    ShareModule,
    ReactiveFormsModule,
    FormErrorModule,
    UtilsModule,
  ],
  exports: [
    RegisterComponent
  ]
})
export class RegisterModule { }
