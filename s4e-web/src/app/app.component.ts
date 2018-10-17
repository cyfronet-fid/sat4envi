import {Component} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import { Tile, Image } from 'ol/layer';
import { ImageWMS, OSM } from 'ol/source';
import {register} from 'ol/proj/proj4';
import proj4 from 'proj4';

@Component({
  selector: 's4e-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(translate: TranslateService) {
    translate.setDefaultLang('pl');
    translate.use('pl');

    proj4.defs('EPSG:3413', '+proj=stere +lat_0=90 +lat_ts=70 +lon_0=-45 +k=1 +x_0=0 +y_0=0 +ellps=WGS84 +datum=WGS84 +units=m +no_defss');
    register(proj4);

    // setting extent like this isn't correct, even though it contains values from EPSG reference, and it doesn't fix the issue with
    // an error during reprojection in the pole region for Tile/OSM layers
    // const proj3413 = getProjection('EPSG:3413');
    // proj3413.setExtent([-5050747.2631, 0.0000, 0.0000, 5050747.2631]);

  }
}
