import {Inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GranuleStore } from './granule.store';
import {Granule, GranuleResponse} from './granule.model';
import {finalize, map} from 'rxjs/operators';
import {IConstants, S4E_CONSTANTS} from '../../../../app.constants';
import {ProductQuery} from '../product/product.query';
import {deserializeJsonResponse} from '../../../../utils/miscellaneous/miscellaneous';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {GranuleQuery} from './granule.query';
import {RecentViewStore} from '../recent-view/recent-view.store';
import {ProductStore} from '../product/product.store';

@Injectable({ providedIn: 'root' })
export class GranuleService {

  constructor(private granuleStore: GranuleStore,
              private granuleQuery: GranuleQuery,
              private productQuery: ProductQuery,
              private productStore: ProductStore,
              private recentViewQuery: RecentViewQuery,
              private recentViewStore: RecentViewStore,
              private http: HttpClient,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants) {
  }

  get(productId: number) {
    this.granuleStore.setLoading(true);
    this.http.get<Granule[]>(`${this.CONSTANTS.apiPrefixV1}/granules/productId/${productId}`).pipe(
      finalize(() => this.granuleStore.setLoading(false)),
      map(data => deserializeJsonResponse(data, GranuleResponse))
    ).subscribe((entities) => {
      this.granuleStore.add(entities);
      const activeView = this.recentViewQuery.getActive();
      let lastGranuleId: number|null = null;

      if (activeView != null && activeView.granuleId) {
        lastGranuleId = activeView.granuleId;
      } else if (this.granuleQuery.getCount() > 0) {
        lastGranuleId = this.granuleQuery.getAll()[this.granuleQuery.getCount() - 1].id;
           }

      this.granuleStore.setActive(lastGranuleId);
      this.recentViewStore.updateActive(active => ({...active, granuleId: lastGranuleId}));
      this.productStore.update(productId, product => ({...product, granuleIds: entities.map(granule => granule.id)}));
    });
  }
}
