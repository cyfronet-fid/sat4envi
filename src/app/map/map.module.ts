import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule} from '@ngx-translate/core';

import {LayersModule} from '../layers/layers.module';

import {MapComponent} from './map.component';

@NgModule({
  declarations: [
    MapComponent,
  ],
  exports: [
    MapComponent,
  ],
  imports: [
    BrowserModule,
    NgbModule,
    TranslateModule.forChild(),

    LayersModule,
  ],
  providers: [],
})
export class MapModule { }
