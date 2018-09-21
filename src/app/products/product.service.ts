import { Injectable } from '@angular/core';
import {Observable, of} from 'rxjs';
import {Granule} from './granule.model';
import {Product} from './product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  constructor() { }

  getProducts(): Observable<Product[]> {
    return of([{
      type: 'rainfall',
    },{
      type: 'clouds',
    }]);
  }

  getGranules(type: string): Observable<Granule[]> {
    const granules: Granule[] = [];
    const layerName = 'test:201807051330_PL_HRV_gtif_mercator';
    for (let i = 0; i < 3; i++) {
      const h = i + 10;
      granules.push({
        type: type,
        timestamp: `20180921T${h}0000`,
        layerName: layerName,
      });
    }
    return of(granules);
  }
}
