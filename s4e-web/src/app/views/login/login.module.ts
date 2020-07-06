import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {LoginComponent} from './login.component';
import {ShareModule} from '../../common/share.module';
import {FormErrorModule} from '../../components/form-error/form-error.module';
import { RecaptchaModule, RecaptchaFormsModule } from 'ng-recaptcha';

@NgModule({
  declarations: [
    LoginComponent
  ],
  imports: [
    CommonModule,
    FormErrorModule,
    ShareModule,
    RecaptchaFormsModule,
    RecaptchaModule,
  ],
  exports: [
    LoginComponent
  ]
})
export class LoginModule { }
