import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RegisterComponent} from './register.component';
import {ReactiveFormsModule} from '@angular/forms';
import {ShareModule} from '../../common/share.module';
import {UtilsModule} from '../../utils/utils.module';
import {FormErrorModule} from '../../components/form-error/form-error.module';
import {RecaptchaFormsModule, RecaptchaModule} from 'ng-recaptcha';
import { RegisterConfirmationComponent } from './register-confirmation/register-confirmation.component';

@NgModule({
  declarations: [
    RegisterComponent,
    RegisterConfirmationComponent
  ],
  imports: [
    CommonModule,
    ShareModule,
    ReactiveFormsModule,
    FormErrorModule,
    UtilsModule,
    RecaptchaFormsModule,
    RecaptchaModule,
  ],
  exports: [
    RegisterComponent,
    RegisterConfirmationComponent
  ]
})
export class RegisterModule { }
