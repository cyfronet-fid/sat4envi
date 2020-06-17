
import { ModalModule } from './../../modal/modal.module';
import { GROUP_FORM_MODAL_ID } from './groups/group-form/group-form-modal.model';
import { SearchModule } from './../../components/search/search.module';
import { AdminDashboardModule } from './admin-dashboard/admin-dashboard.module';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {settingsRoutes} from './settings.routes';
import {SettingsComponent} from './settings.component';
import {ProfileModule} from './profile/profile.module';
import {InstitutionProfileModule} from './intitution-profile/institution-profile.module';
import {DashboardModule} from './dashboard/dashboard.module';
import {PeopleModule} from './people/people.module';
import {ReactiveFormsModule} from '@angular/forms';
import {S4EFormsModule} from '../../form/form.module';
import {ManageInstitutionsModule} from './manage-institutions/manage-institutions.module';
import {GroupsModule} from './groups/groups.module';

@NgModule({
  declarations: [
    SettingsComponent
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
    GroupsModule,
    RouterModule.forChild(settingsRoutes),
    ManageInstitutionsModule,
    SearchModule,
    ModalModule
  ]
})
export class SettingsModule {
}
