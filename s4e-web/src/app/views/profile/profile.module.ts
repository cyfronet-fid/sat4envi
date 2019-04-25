import {NgModule} from '@angular/core';

import {ShareModule} from '../../common/share.module';
import {ProfileComponent} from './profile.component';

@NgModule({
  declarations: [
    ProfileComponent,
  ],
  imports: [
    ShareModule,
  ]
})
export class ProfileModule { }
