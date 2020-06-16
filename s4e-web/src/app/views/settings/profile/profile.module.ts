import {NgModule} from '@angular/core';

import {ShareModule} from '../../../common/share.module';
import {ProfileComponent} from './profile.component';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { S4EFormsModule } from 'src/app/form/form.module';

@NgModule({
  declarations: [
    ProfileComponent,
    ChangePasswordComponent,
  ],
  imports: [
    ShareModule,
    S4EFormsModule
  ],
  exports: [
    ProfileComponent,
    ChangePasswordComponent
  ]
})
export class ProfileModule { }
