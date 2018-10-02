import {NgModule} from '@angular/core';
import {CommonModule} from '../common.module';

import {MapComponent} from './map.component';
import {ViewManagerComponent} from './view-manager.component';

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
  ],
  providers: [],
})
export class MapModule { }
