import { ChangePasswordComponent } from './profile/change-password/change-password.component';
import { InstitutionFormComponent } from './manage-institutions/institution-form/institution-form.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { Routes} from '@angular/router';
import {ProfileComponent} from './profile/profile.component';
import {SettingsComponent} from './settings.component';
import {GroupListComponent} from './groups/group-list/group-list.component';
import {InstitutionProfileComponent} from './intitution-profile/institution-profile.component';
import {PersonListComponent} from './people/person-list/person-list.component';
import {IsLoggedIn} from '../../utils/auth-guard/auth-guard.service';
import {PersonFormComponent} from './people/person-form/person-form.component';
import {GroupFormComponent} from './groups/group-form/group-form.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {IsManagerGuard} from './guards/is-manager/is-manager.guard';
import { adminDashboardMatcher, managerDashboardMatcher } from 'src/app/utils';
import {InstitutionListComponent} from './manage-institutions/institution-list/institution-list.component';

export const settingsRoutes: Routes = [
  {
    path: 'settings',
    component: SettingsComponent,
    canActivate: [IsLoggedIn],
    children: [
      {
        matcher: adminDashboardMatcher,
        component: AdminDashboardComponent
      },
      {
        matcher: managerDashboardMatcher,
        component: DashboardComponent
      },
      {
        path: 'add-institution',
        component: InstitutionFormComponent,
        canActivate: [IsManagerGuard],
        data: {
          isEditMode: false
        }
      },
      {
        path: 'edit-institution',
        component: InstitutionFormComponent,
        canActivate: [IsManagerGuard],
        data: {
          isEditMode: true
        }
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
        path: 'groups',
        component: GroupListComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: 'groups/:groupSlug',
        component: GroupFormComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: 'institution',
        component: InstitutionProfileComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: 'institutions',
        component: InstitutionListComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: 'people',
        component: PersonListComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: 'people/add',
        component: PersonFormComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: 'people/:email',
        component: PersonFormComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: '**',
        redirectTo: 'profile',
        pathMatch: 'full',
      },
    ]
  },
];
