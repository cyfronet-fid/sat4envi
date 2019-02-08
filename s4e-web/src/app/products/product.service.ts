import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {map} from 'rxjs/operators';
import {parse} from 'date-fns';

import {Granule} from './granule.model';
import {Product} from './product.model';
import {apiPrefixV1, backendDateFormat} from '../constants';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  constructor(private http: HttpClient) { }

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${apiPrefixV1}/products`);
  }

  getGranules(productId: number): Observable<Granule[]> {
    return this.http.get<Granule[]>(`${apiPrefixV1}/granules/productId/${productId}`).pipe(map(granules => {
      return granules.map(granule => {
        granule.timestampDate = parse(granule.timestamp, backendDateFormat, new Date());
        return granule;
      });
    }));
  }
}
