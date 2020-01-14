import {ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot, Routes} from '@angular/router';
import {ProfileComponent} from './profile/profile.component';
import {SettingsComponent} from './settings.component';
import {GroupListComponent} from './groups/group-list/group-list.component';
import {InstitutionProfileComponent} from './intitution-profile/institution-profile.component';
import {PersonListComponent} from './people/person-list/person-list.component';
import {IsLoggedIn} from '../../utils/auth-guard/auth-guard.service';
import {PersonFormComponent} from './people/person-form/person-form.component';
import {GroupFormComponent} from './groups/group-form/group-form.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {combineLatest, Observable} from 'rxjs';
import {filter, map, switchMap, take} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {InstitutionService} from './state/institution.service';
import {InstitutionQuery} from './state/institution.query';
import {Injectable} from '@angular/core';
import {IsManagerGuard} from './guards/is-manager/is-manager.guard';

export const settingsRoutes: Routes = [
  {
    path: 'settings',
    component: SettingsComponent,
    canActivate: [IsLoggedIn],
    children: [
      {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [IsManagerGuard]
      },
      {
        path: 'profile',
        component: ProfileComponent,
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
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
    ]
  },
];
