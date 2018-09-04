import {NgModule} from '@angular/core';
import {LayersComponent} from './layers.component';
import {BrowserModule} from '@angular/platform-browser';

@NgModule({
  declarations: [
    LayersComponent
  ],
  exports: [
    LayersComponent
  ],
  imports: [
    BrowserModule,
  ],
  providers: [],
})
export class LayersModule { }
