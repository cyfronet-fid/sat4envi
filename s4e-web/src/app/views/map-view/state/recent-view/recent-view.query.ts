import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {RecentViewState, RecentViewStore} from './recent-view.store';
import {ICompleteRecentView, RecentView} from './recent-view.model';
import {Observable, of} from 'rxjs';
import {ProductQuery} from '../product/product.query';
import {GranuleQuery} from '../granule/granule.query';
import {distinctUntilChanged, filter, map, mergeMap, tap} from 'rxjs/operators';
import {Granule} from '../granule/granule.model';

@Injectable({
  providedIn: 'root'
})
export class RecentViewQuery extends QueryEntity<RecentViewState, RecentView> {

  constructor(protected store: RecentViewStore, private productQuery: ProductQuery, private granuleQuery: GranuleQuery) {
    super(store);
  }

  selectViewsWithData(): Observable<ICompleteRecentView[]> {
    return this.selectAll().pipe(
      map(views => views.map(view => ({
        ...view,
        activeGranule: this.granuleQuery.getEntity(view.granuleId),
        activeProduct: this.productQuery.getEntity(view.productId)
      })))
    );
  }

  selectActiveViewGranules(): Observable<Granule[]> {
    return this.selectActive().pipe(
      filter(view => view != null),
      mergeMap(view => this.productQuery.selectEntity(view.productId)),
      mergeMap(product => product ? this.granuleQuery.selectMany(product.granuleIds) : of([])),
    );
  }

  selectActiveGranule(): Observable<Granule> {
    return this.selectActive().pipe(
      filter(view => view != null),
      mergeMap(view => view.granuleId ? this.granuleQuery.selectEntity(view.granuleId) : of(null)),
      distinctUntilChanged()
    );
  }
}
