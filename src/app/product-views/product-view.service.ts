import { Injectable } from '@angular/core';

import {ProductView} from './product-view.model';

@Injectable({
  providedIn: 'root'
})
export class ProductViewService {
  constructor() { }

  getViews(): Array<ProductView> {
    return [{
      id: 'opady',
      layers: [{
        id: 1,
        timestamp: '20180824T100000',
        layerName: 'test:201807051330_PL_HRV_gtif_mercator'
      }, {
        id: 2,
        timestamp: '20180824T110000',
        layerName: 'test:201807051330_PL_HRV_gtif_mercator'
      }, {
        id: 3,
        timestamp: '20180824T120000',
        layerName: 'test:201807051330_PL_HRV_gtif_mercator'
      }]
    }, {
      id: 'zachmurzenie',
      layers: [{
        id: 4,
        timestamp: '20180824T200000',
        layerName: 'test:201807051330_PL_HRV_gtif_mercator'
      }, {
        id: 5,
        timestamp: '20180824T210000',
        layerName: 'test:201807051330_PL_HRV_gtif_mercator'
      }, {
        id: 6,
        timestamp: '20180824T220000',
        layerName: 'test:201807051330_PL_HRV_gtif_mercator'
      }]
    }];
  }
}
