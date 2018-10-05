import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';

import {Granule} from './granule.model';
import {Product} from './product.model';
import {apiPrefix} from '../constants';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  constructor(private http: HttpClient) { }

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${apiPrefix}/products`);
  }

  getGranules(productId: number): Observable<Granule[]> {
    return this.http.get<Granule[]>(`${apiPrefix}/granules/productId/${productId}`);
  }
}
