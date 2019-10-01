import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductTypeStore} from './product-type.store';
import {ProductType} from './product-type.model';
import {finalize} from 'rxjs/operators';
import {ProductService} from '../product/product.service';
import {ProductTypeQuery} from './product-type.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {ProductStore} from '../product/product.store';
import {LegendService} from '../legend/legend.service';

@Injectable({providedIn: 'root'})
export class ProductTypeService {

  constructor(private productTypeStore: ProductTypeStore,
              private productStore: ProductStore,
              private CONFIG: S4eConfig,
              private http: HttpClient,
              private legendService: LegendService,
              private productTypeQuery: ProductTypeQuery,
              private productService: ProductService) {
  }

  get() {
    this.productStore.setLoading(true);
    this.http.get<ProductType[]>(`${this.CONFIG.apiPrefixV1}/productTypes`).pipe(
      finalize(() => this.productStore.setLoading(false)),
    ).subscribe(data => this.productTypeStore.set(data));
  }

  setActive(productTypeId: number | null) {
    this.legendService.set({
      url: '/assets/images/legend.svg',
      type: 'gradient',
      leftDescription: {
        0.1: '10%',
        0.2: '20%',
        0.3: '30%',
        0.4: '40%',
        0.5: '50%',
        0.6: '60%',
        0.7: '70%',
        0.8: '80%',
        0.9: '90%',
        1.0: '100%'
      },
      rightDescription: {
        0.1: 'A',
        0.2: 'B',
        0.3: 'C',
        0.4: 'D',
        0.5: 'E',
        0.6: 'F',
        0.7: 'G',
        0.8: 'H',
        0.9: 'I',
        1.0: 'J'
      },
      metricBottom: '',
      metricTop: ''
    });
    if (productTypeId != null && this.productTypeQuery.getActiveId() !== productTypeId) {
      if (this.productTypeQuery.getEntity(productTypeId).productIds === undefined) {
        this.productService.get(productTypeId);
      }
      this.productTypeStore.setActive(productTypeId);
    } else {
      this.productTypeStore.setActive(null);
      this.productStore.setActive(null);
    }
  }
}
