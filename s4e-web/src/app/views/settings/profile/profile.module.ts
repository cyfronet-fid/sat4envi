import { TilesDashboardModule } from './../../../components/tiles-dashboard/tiles-dashboard.module';
import {NgModule} from '@angular/core';

import {ShareModule} from '../../../common/share.module';
import {ProfileComponent} from './profile.component';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { S4EFormsModule } from 'src/app/form/form.module';
import { FormErrorModule } from 'src/app/components/form-error/form-error.module';
import {GenericListViewModule} from '../components/generic-list-view/generic-list-view.module';

@NgModule({
  declarations: [
    ProfileComponent,
    ChangePasswordComponent,
  ],
    imports: [
        ShareModule,
        S4EFormsModule,
        FormErrorModule,
        TilesDashboardModule,
        GenericListViewModule
    ],
  exports: [
    ProfileComponent,
    ChangePasswordComponent
  ]
})
export class ProfileModule { }
