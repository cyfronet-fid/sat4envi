import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import {LayersComponent} from './layers.component';
import {LayersService} from './layers.service';
import {LayerComponent} from './layer.component';

@NgModule({
  declarations: [
    LayersComponent,
    LayerComponent,
  ],
  exports: [
    LayersComponent,
    LayerComponent,
  ],
  imports: [
    BrowserModule,
    NgbModule,
  ],
  providers: [],
})
export class LayersModule { }
