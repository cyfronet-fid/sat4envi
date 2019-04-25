import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RegisterComponent} from './register.component';
import {ReactiveFormsModule} from '@angular/forms';
import {ShareModule} from '../../common/share.module';
import {ErrorKeysPipe} from '../../utils/error-keys/error-keys.pipe';
import {UtilsModule} from '../../utils/utils.module';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';

@NgModule({
  declarations: [
    RegisterComponent,
  ],
  imports: [
    CommonModule,
    ShareModule,
    ReactiveFormsModule,
    UtilsModule,
  ],
  exports: [
    RegisterComponent
  ]
})
export class RegisterModule { }
