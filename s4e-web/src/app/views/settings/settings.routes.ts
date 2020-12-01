import {InstitutionFormComponent} from './manage-institutions/institution-form/institution-form.component';
import {ChangePasswordComponent} from './profile/change-password/change-password.component';
import {Routes} from '@angular/router';
import {ProfileComponent} from './profile/profile.component';
import {SettingsComponent} from './settings.component';
import {IsLoggedIn} from '../../utils/auth-guard/auth-guard.service';
import {DashboardComponent} from './dashboard/dashboard.component';
import {PersonListComponent} from './people/person-list/person-list.component';
import {IsManagerGuard} from './guards/is-manager/is-manager.guard';
import {InstitutionListComponent} from './manage-institutions/institution-list/institution-list.component';
import {InstitutionProfileComponent} from './intitution-profile/institution-profile.component';
import {
  ADD_INSTITUTION_PATH,
  EDIT_INSTITUTION_PATH,
  INSTITUTION_PROFILE_PATH,
  INSTITUTIONS_LIST_PATH,
  SETTINGS_PATH
} from './settings.breadcrumbs';
import {multipleInstitutionAdminDashboardMatcher, singleInstitutionAdminDashboardMatcher} from './dashboards.routes';
import {ManageAuthoritiesComponent} from './manage-authorities/manage-authorities.component';
import {IsAdminGuard} from './guards/is-admin.guard';
import {GLOBAL_OVERLAYS_PATH, INSTITUTION_OVERLAYS_PATH, WmsOverlaysComponent} from './wms-overlays/wms-overlays.component';


export const settingsRoutes: Routes = [
  {
    path: SETTINGS_PATH,
    component: SettingsComponent,
    canActivate: [IsLoggedIn],
    children: [
      {
        matcher: multipleInstitutionAdminDashboardMatcher,
        redirectTo: INSTITUTIONS_LIST_PATH
      },
      {
        matcher: singleInstitutionAdminDashboardMatcher,
        component: DashboardComponent
      },
      {
        path: 'manage-authorities',
        component: ManageAuthoritiesComponent
      },
      {
        path: INSTITUTIONS_LIST_PATH,
        component: InstitutionListComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: INSTITUTION_PROFILE_PATH,
        component: InstitutionProfileComponent,
        canActivate: [IsLoggedIn]
      },

      {
        path: ADD_INSTITUTION_PATH,
        component: InstitutionFormComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: INSTITUTION_OVERLAYS_PATH,
        component: WmsOverlaysComponent
      },
      {
        path: GLOBAL_OVERLAYS_PATH,
        component: WmsOverlaysComponent,
        canActivate: [IsAdminGuard]
      },
      {
        path: EDIT_INSTITUTION_PATH,
        component: InstitutionFormComponent,
        canActivate: [IsManagerGuard]
      },

      {
        path: 'people',
        component: PersonListComponent,
        canActivate: [IsManagerGuard]
      },

      {
        path: 'profile',
        component: ProfileComponent
      },
      {
        path: 'change-password',
        component: ChangePasswordComponent
      },

      {
        path: '**',
        redirectTo: 'profile',
        pathMatch: 'full',
      },
    ]
  },
];
