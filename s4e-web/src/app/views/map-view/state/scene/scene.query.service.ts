import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { SceneStore, SceneState } from './scene.store.service';
import { Scene } from './scene.model';
import {combineLatest, Observable} from 'rxjs';
import {IUILayer} from '../common.model';
import {map} from 'rxjs/operators';
// import {combineLatest, Observable, of} from 'rxjs';
// import {ICompleteRecentView} from '../recent-view/recent-view.model';
// import {distinctUntilChanged, map, mergeMap} from 'rxjs/operators';
// import {IUILayer} from '../common.model';

@Injectable({
  providedIn: 'root'
})
export class SceneQuery extends QueryEntity<SceneState, Scene> {

  constructor(protected store: SceneStore) {
    super(store);
  }
  //
  // selectViewsWithData(): Observable<ICompleteRecentView[]> {
  //   return this.selectAll().pipe(
  //     map(views => views.map(view => ({
  //       ...view,
  //       activeProduct: this.productQuery.getEntity(view.productId),
  //       activeProductType: this.productTypeQuery.getEntity(view.productTypeId)
  //     })))
  //   );
  // }
  //
  // selectActiveViewProducts(): Observable<Scene[]> {
  //   return this.selectActive().pipe(
  //     mergeMap(view => view != null ? this.productTypeQuery.selectEntity(view.productTypeId) : of(null)),
  //     mergeMap(product => product ? this.productQuery.selectMany(product.productIds) : of([])),
  //   );
  // }
  //
  // selectActiveProduct(): Observable<Scene> {
  //   return this.selectActive().pipe(
  //     mergeMap(view => (view != null && view.productId) ? this.productQuery.selectEntity(view.productId) : of(null)),
  //     distinctUntilChanged()
  //   );
  // }
  //
  //
}
