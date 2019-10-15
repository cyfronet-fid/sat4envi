import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {LoginComponent} from './login.component';
import {ReactiveFormsModule} from '@angular/forms';
import {ShareModule} from '../../common/share.module';
import {FormErrorModule} from '../../components/form-error/form-error.module';

@NgModule({
  declarations: [
    LoginComponent
  ],
  imports: [
    CommonModule,
    FormErrorModule,
    ShareModule
  ],
  exports: [
    LoginComponent
  ]
})
export class LoginModule { }
