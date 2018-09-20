import {Component} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import { Tile, Image } from 'ol/layer';
import { ImageWMS, OSM } from 'ol/source';

@Component({
  selector: 's4e-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(translate: TranslateService) {
    translate.setDefaultLang('pl');
    translate.use('pl');
  }
}
