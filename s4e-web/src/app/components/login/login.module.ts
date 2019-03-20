import { NgModule } from '@angular/core';

import {CommonModule} from '../../common.module';
import {LoginComponent} from './login.component';
import {ConstantsProvider} from '../../app.constants';

@NgModule({
  declarations: [
    LoginComponent,
  ],
  exports: [
    LoginComponent
  ],
  imports: [
    CommonModule
  ]
})
export class LoginModule { }
