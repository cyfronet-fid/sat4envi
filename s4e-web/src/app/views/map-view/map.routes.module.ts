import { Routes } from '@angular/router';
import { MapViewComponent } from './map-view.component';
import { environment } from 'src/environments/environment';
import { IsLoggedIn } from 'src/app/utils/auth-guard/auth-guard.service';
import { ViewManagerComponent } from './view-manager/view-manager.component';
import { SentinelSearchComponent } from './sentinel-search/sentinel-search.component';

export const routes: Routes = [
  {
      path: 'map',
      component: MapViewComponent,
      canActivate: environment.inviteOnly ? [IsLoggedIn] : [],
      children: [
          {
              path: 'products',
              component: ViewManagerComponent
          },
          {
              path: 'sentinel-search',
              component: SentinelSearchComponent
          },
          {
              path: '',
              pathMatch: 'prefix',
              redirectTo: '/map/products'
          },
      ]
  }
];
