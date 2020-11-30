import {InstitutionFormComponent} from './manage-institutions/institution-form/institution-form.component';
import {ChangePasswordComponent} from './profile/change-password/change-password.component';
import {Routes} from '@angular/router';
import {ProfileComponent} from './profile/profile.component';
import {SettingsComponent} from './settings.component';
import {IsLoggedIn} from '../../utils/auth-guard/auth-guard.service';
import {AdminDashboardComponent} from './admin-dashboard/admin-dashboard.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {PersonListComponent} from './people/person-list/person-list.component';
import {IsManagerGuard} from './guards/is-manager/is-manager.guard';
import {InstitutionListComponent} from './manage-institutions/institution-list/institution-list.component';
import {InstitutionProfileComponent} from './intitution-profile/institution-profile.component';
import {ADD_INSTITUTION_PATH, INSTITUTION_PROFILE_PATH, INSTITUTIONS_LIST_PATH, SETTINGS_PATH} from './settings.breadcrumbs';
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
        component: AdminDashboardComponent,
        data: {
          isAdminDashboard: true,
          breadcrumbs: [
            {
              label: 'Tablica administratora',
              url: 'dashboard'
            }
          ]
        }
      },
      {
        matcher: singleInstitutionAdminDashboardMatcher,
        component: DashboardComponent,
        data: {
          isAdminDashboard: false,
          breadcrumbs: [
            {
              label: 'Tablica administratora',
              url: 'dashboard'
            }
          ]
        }
      },
      {
        path: 'manage-authorities',
        component: ManageAuthoritiesComponent
      },
      {
        path: INSTITUTIONS_LIST_PATH,
        component: InstitutionListComponent,
        canActivate: [IsManagerGuard],
        data: {
          breadcrumbs: [
            {
              label: 'Lista instytucji'
            }
          ]
        }
      },
      {
        path: INSTITUTION_PROFILE_PATH,
        component: InstitutionProfileComponent,
        canActivate: [IsLoggedIn],
        data: {
          breadcrumbs: [
            {
              label: 'Profil instytucji'
            }
          ]
        }
      },

      {
        path: ADD_INSTITUTION_PATH,
        component: InstitutionFormComponent,
        canActivate: [IsManagerGuard],
        data: {
          isEditMode: false,
          breadcrumbs: [
            {
              label: 'Lista instytucji',
              url: INSTITUTIONS_LIST_PATH
            },
            {
              label: 'Dodaj instytucję'
            }
          ]
        }
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
        path: 'edit-institution',
        component: InstitutionFormComponent,
        canActivate: [IsManagerGuard],
        data: {
          isEditMode: true,
          breadcrumbs: [
            {
              label: 'Profil instytucji',
              url: INSTITUTION_PROFILE_PATH
            },
            {
              label: 'Edytuj instytucję',
            }
          ]
        }
      },

      {
        path: 'people',
        component: PersonListComponent,
        canActivate: [IsManagerGuard],
        data: {
          breadcrumbs: [
            {
              label: 'Ludzie'
            }
          ]
        }
      },

      {
        path: 'profile',
        component: ProfileComponent,
        data: {
          breadcrumbs: [
            {
              label: 'Mój profil'
            }
          ]
        }
      },
      {
        path: 'change-password',
        component: ChangePasswordComponent,
        data: {
          breadcrumbs: [
            {
              label: 'Zmiana hasła'
            }
          ]
        }
      },

      {
        path: '**',
        redirectTo: 'profile',
        pathMatch: 'full',
      },
    ]
  },
];
