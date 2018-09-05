import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {LayersComponent} from './layers.component';
import {LayersService} from './layers.service';

@NgModule({
  declarations: [
    LayersComponent,
  ],
  exports: [
    LayersComponent,
  ],
  imports: [
    BrowserModule,
  ],
  providers: [],
})
export class LayersModule { }
