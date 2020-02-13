import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {ProductState, ProductStore} from './product.store';
import {Product} from './product.model';
import {combineLatest, Observable} from 'rxjs';
import {IUILayer} from '../common.model';
import {map} from 'rxjs/operators';
import {SceneQuery} from '../scene/scene.query.service';

@Injectable({
  providedIn: 'root'
})
export class ProductQuery extends QueryEntity<ProductState, Product> {
  constructor(protected store: ProductStore, private sceneQuery: SceneQuery) {
    super(store);
  }

  public selectAllAsUILayer(): Observable<IUILayer[]> {
    return combineLatest(this.selectAll(), this.selectActiveId()).pipe(
      map(([products, activeId]) => products.map(pt => ({cid: pt.id, caption: pt.displayName, active: pt.id === activeId})))
    );
  }

  selectSelectedDate() {
    return this.select(state => state.ui.selectedDate);
  }

  selectAvailableDates() {
    return this.select(state => state.ui.availableDays);
  }
}
