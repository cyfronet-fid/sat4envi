import {ModalModule} from '../../modal/modal.module';
import {SearchModule} from '../../components/search/search.module';
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
import {SETTINGS_PATH} from './settings.breadcrumbs';
import { EventsModule } from 'src/app/utils/search/events.module';
import {ManageAuthoritiesComponent} from './manage-authorities/manage-authorities.component';
import {GenericListViewModule} from './components/generic-list-view/generic-list-view.module';
import {OverlayListModule} from '../../components/overlay-list/overlay-list.module';
import {WmsOverlaysComponent} from './wms-overlays/wms-overlays.component';

@NgModule({
  declarations: [
    SettingsComponent,
    ManageAuthoritiesComponent,
    WmsOverlaysComponent
  ],
  imports: [
    CommonModule,
    S4EFormsModule,
    ReactiveFormsModule,
    ProfileModule,
    InstitutionProfileModule,
    DashboardModule,
    PeopleModule,
    RouterModule.forChild(settingsRoutes),
    ManageInstitutionsModule,
    SearchModule,
    ModalModule,
    EventsModule,
    GenericListViewModule,
    OverlayListModule
  ]
})
export class SettingsModule {}
