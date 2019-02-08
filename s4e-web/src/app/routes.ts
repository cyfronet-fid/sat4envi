import {Routes} from '@angular/router';

import {MapComponent} from './map/map.component';
import {ProfileComponent} from './profile/profile.component';

export const routes: Routes = [
  {
    path: 'map',
    component: MapComponent,
  }, {
    path: 'profile',
    component: ProfileComponent,
  }, {
    path: '**',
    redirectTo: '/map',
    pathMatch: 'full',
  },
];
