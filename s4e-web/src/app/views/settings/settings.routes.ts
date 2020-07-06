import { GroupListComponent } from './groups/group-list/group-list.component';
import { InstitutionFormComponent } from './manage-institutions/institution-form/institution-form.component';
import { ChangePasswordComponent } from './profile/change-password/change-password.component';
import { IBreadcrumb } from './breadcrumb/breadcrumb.model';
import { Routes} from '@angular/router';
import {ProfileComponent} from './profile/profile.component';
import {SettingsComponent} from './settings.component';
import {IsLoggedIn} from '../../utils/auth-guard/auth-guard.service';
import { adminDashboardMatcher, managerDashboardMatcher } from './dashboards.routes';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { PersonListComponent } from './people/person-list/person-list.component';
import { IsManagerGuard } from './guards/is-manager/is-manager.guard';
import { InstitutionListComponent } from './manage-institutions/institution-list/institution-list.component';
import { InstitutionProfileComponent } from './intitution-profile/institution-profile.component';

export const userSettingsRoutes: Routes = [

];

export const SETTINGS_PATH = 'settings';
export const ADD_INSTITUTION_PATH = 'add-institution';
export const INSTITUTIONS_LIST_PATH = 'institutions';
export const INSTITUTION_PROFILE_PATH = 'institution';
export const settingsRoutes: Routes = [
  {
    path: SETTINGS_PATH,
    component: SettingsComponent,
    canActivate: [IsLoggedIn],
    children: [
      {
        matcher: adminDashboardMatcher,
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
        matcher: managerDashboardMatcher,
        component: DashboardComponent,
        data: {
          isAdminDashboard: false,
          breadcrumbs: [
            {
              label: 'Tablica menedżera',
              url: 'dashboard'
            }
          ]
        }
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
        canActivate: [IsManagerGuard],
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
        path: 'groups',
        component: GroupListComponent,
        canActivate: [IsManagerGuard],
        data: {
          breadcrumbs: [
            {
              label: 'Grupy'
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
