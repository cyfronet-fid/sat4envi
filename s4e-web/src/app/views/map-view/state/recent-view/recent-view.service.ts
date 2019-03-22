import { Injectable } from '@angular/core';
import { ID } from '@datorama/akita';
import { HttpClient } from '@angular/common/http';
import { RecentViewStore } from './recent-view.store';
import {ProductService} from '../product/product.service';
import {RecentViewQuery} from './recent-view.query';

@Injectable({ providedIn: 'root' })
export class RecentViewService {

  constructor(private recentViewStore: RecentViewStore,
              private recentViewQuery: RecentViewQuery,
              private http: HttpClient,
              private productService: ProductService) {
  }



  remove(id: ID) {
    this.recentViewStore.setActive({next: true});
    this.recentViewStore.remove(id);
  }

  setActive(viewId: number) {
    this.recentViewStore.setActive(viewId);
  }

  updateActiveViewProduct(productId: number) {
    this.recentViewStore.updateActive(active => ({...active, productId}));
  }
}
