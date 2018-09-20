import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule} from '@ngx-translate/core';

import {LayersComponent} from './layers.component';
import {LayerComponent} from './layer.component';

export { LayersComponent, LayerComponent };
export {LayersService} from './layers.service';
export {Layer} from './layer.model';

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
    TranslateModule.forChild(),
  ],
  providers: [],
})
export class LayersModule { }
