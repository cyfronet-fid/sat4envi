import {BreadcrumbService} from './breadcrumb/breadcrumb.service';
import {ModalModule} from '../../modal/modal.module';
import {BreadcrumbModule} from './breadcrumb/breadcrumb.module';
import {SearchModule} from '../../components/search/search.module';
import {AdminDashboardModule} from './admin-dashboard/admin-dashboard.module';
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
import {SETTINGS_PATH} from './settings.breadcrumbs';
import { EventsModule } from 'src/app/utils/dropdown/events.module';

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
    ModalModule,
    BreadcrumbModule,
    EventsModule
  ]
})
export class SettingsModule {
  constructor(private _breadcrumbService: BreadcrumbService) {
    const childrenRoutes = settingsRoutes.find(route => route.path === SETTINGS_PATH).children;
    this._breadcrumbService.registerRoutes(childrenRoutes, SETTINGS_PATH);
    this._breadcrumbService.setMainRoutes(settingsRoutes);
  }
}
