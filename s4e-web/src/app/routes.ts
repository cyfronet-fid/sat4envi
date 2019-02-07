import {Routes} from '@angular/router';

import {MapComponent} from './map/map.component';

export const routes: Routes = [
  {
    path: 'map',
    component: MapComponent,
  }, {
    path: '**',
    redirectTo: '/map',
    pathMatch: 'full',
  },
];
