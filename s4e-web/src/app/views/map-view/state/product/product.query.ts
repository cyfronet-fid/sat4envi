import {Injectable} from '@angular/core';
import {EntityUIQuery, QueryEntity} from '@datorama/akita';
import {ProductStore} from './product.store';
import {Product, PRODUCT_MODE_FAVOURITE, PRODUCT_MODE_QUERY_KEY, ProductState, ProductUIState} from './product.model';
import {combineLatest, Observable} from 'rxjs';
import {IUILayer} from '../common.model';
import {map} from 'rxjs/operators';
import {SceneQuery} from '../scene/scene.query.service';
import {RouterQuery} from '@datorama/akita-ng-router-store';

@Injectable({
  providedIn: 'root'
})
export class ProductQuery extends QueryEntity<ProductState, Product> {
  public readonly ui: EntityUIQuery<ProductUIState>;

  constructor(protected store: ProductStore,
              private routerQuery: RouterQuery,
              private sceneQuery: SceneQuery) {
    super(store);
    this.createUIQuery();
  }

  public selectFavourites(): Observable<Product[]> {
    return this.selectAll().pipe(map(products => products.filter(p => p.favourite)));
  }

  public selectFavouritesCount(): Observable<number> {
    return this.selectFavourites().pipe(map(products => products.length));
  }

  public selectAllFilteredAsUILayer(): Observable<IUILayer[]> {
    return combineLatest(
      this.routerQuery.selectQueryParams(PRODUCT_MODE_QUERY_KEY),
      this.selectAllAsUILayer()
    ).pipe(
      map(([mode, layers]) => {
        switch(mode) {
          case PRODUCT_MODE_FAVOURITE: {
            return layers.filter(layer => layer.favourite);
          }
          default: {
            return layers;
          }
        }
      })
    );
  }

  public selectAllAsUILayer(): Observable<IUILayer[]> {
    return combineLatest(this.selectAll(), this.ui.selectAll(), this.selectActiveId())
      .pipe(
        map(([products, productsUi, activeId]) => products
          .map((pt, i) => ({
            cid: pt.id,
            caption: pt.displayName,
            active: pt.id === activeId,
            favourite: pt.favourite,
            isLoading: productsUi[i].isLoading,
            isFavouriteLoading: productsUi[i].isFavouriteLoading
          }))
        )
      );
  }

  selectSelectedDate() {
    return this.select(state => state.ui.selectedDate);
  }

  selectAvailableDates() {
    return this.select(state => state.ui.availableDays);
  }

  selectIsFavouriteMode(): Observable<boolean> {
    return this.routerQuery.selectQueryParams(PRODUCT_MODE_QUERY_KEY)
      .pipe(map(param => param === PRODUCT_MODE_FAVOURITE));
  }
}
