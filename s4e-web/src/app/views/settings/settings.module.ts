import { AdminDashboardModule } from './admin-dashboard/admin-dashboard.module';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {settingsRoutes} from './settings.routes';
import {SettingsComponent} from './settings.component';
import {ProfileModule} from './profile/profile.module';
import {InstitutionProfileModule} from './intitution-profile/institution-profile.module';
import {DashboardModule} from './dashboard/dashboard.module';
import {GroupFormComponent} from './groups/group-form/group-form.component';
import {GroupListComponent} from './groups/group-list/group-list.component';
import {PeopleModule} from './people/people.module';
import {ReactiveFormsModule} from '@angular/forms';
import {S4EFormsModule} from '../../form/form.module';
import { ParentInstitutionModalModule } from './manage-institutions/parent-institution-modal/parent-institution-modal.module';

@NgModule({
  declarations: [
    SettingsComponent,
    GroupListComponent,
    GroupFormComponent,
  ],
  imports: [
    CommonModule,
    S4EFormsModule,
    ReactiveFormsModule,
    ProfileModule,
    InstitutionProfileModule,
    DashboardModule,
    AdminDashboardModule,
    PeopleModule,
    RouterModule.forChild(settingsRoutes),
    ParentInstitutionModalModule
  ]
})
export class SettingsModule {
}
