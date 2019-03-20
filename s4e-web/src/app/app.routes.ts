import {Routes} from '@angular/router';

import {MapViewComponent} from './views/map-view/map-view.component';
import {ProfileComponent} from './views/profile/profile.component';

export enum RouteId {
  map,
  profile
}

/**
 * All data in route/data should implement this interface
 */
export interface RouteData {
  routeId: RouteId;
}

export const appRoutes: Routes = [
  {
    path: '',
    component: MapViewComponent,
    data: {routeId: RouteId.map }
  }, {
    path: 'profile',
    component: ProfileComponent,
    data: {routeId: RouteId.profile}
  }, {
    path: '**',
    redirectTo: '/',
    pathMatch: 'full',
  },
];
