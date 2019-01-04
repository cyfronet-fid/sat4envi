import {NgModule} from '@angular/core';
import {CommonModule} from '../common.module';

import {MapComponent} from './map.component';
import {ViewManagerComponent} from './view-manager.component';
import {LoginModule} from '../login/login.module';

@NgModule({
  declarations: [
    MapComponent,
    ViewManagerComponent,
  ],
  exports: [
    MapComponent,
  ],
  imports: [
    CommonModule,
    LoginModule,
  ],
  providers: [],
})
export class MapModule { }
