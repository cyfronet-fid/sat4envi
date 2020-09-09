import {Injectable} from '@angular/core';
import {EntityUIQuery, QueryEntity} from '@datorama/akita';
import {ProductStore} from './product.store';
import {
  AVAILABLE_TIMELINE_RESOLUTIONS, DEFAULT_TIMELINE_RESOLUTION,
  Product,
  PRODUCT_MODE_FAVOURITE,
  PRODUCT_MODE_QUERY_KEY,
  ProductState,
  ProductUIState,
  TIMELINE_RESOLUTION_QUERY_KEY
} from './product.model';
import {combineLatest, Observable} from 'rxjs';
import {IUILayer} from '../common.model';
import {map} from 'rxjs/operators';
import {RouterQuery} from '@datorama/akita-ng-router-store';

@Injectable({
  providedIn: 'root'
})
export class ProductQuery extends QueryEntity<ProductState, Product> {
  public readonly ui: EntityUIQuery<ProductUIState>;

  constructor(
    protected _store: ProductStore,
    private _routerQuery: RouterQuery
  ) {
    super(_store);
    this.createUIQuery();
  }

  public selectFavourites(): Observable<Product[]> {
    return this.selectAll().pipe(map(products => products.filter(p => p.favourite)));
  }

  public selectFavouritesCount(): Observable<number> {
    return this.selectFavourites().pipe(map(products => products.length));
  }

  public selectGroupedProducts(): Observable<IUILayer[][]> {
    return this.selectAllFilteredAsUILayer()
      .pipe(map(products => Object.values<IUILayer[]>(
        products
          .reduce((accumulator, layer) => {
              const category = layer.category;
              if (!category) {
                return accumulator;
              }

            Object.keys(accumulator).some(key => key === category.id.toString())
                ?  accumulator[category.id].push(layer)
                : accumulator[category.id] = [layer];

              return accumulator;
            },
            {}
          )
        )
        .filter(categoryProducts => !!categoryProducts && categoryProducts.length > 0)
      ));
  }

  public selectAllFilteredAsUILayer(): Observable<IUILayer[]> {
    return combineLatest(
      this._routerQuery.selectQueryParams(PRODUCT_MODE_QUERY_KEY),
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
            label: pt.displayName,
            active: pt.id === activeId,
            category: pt.productCategory,
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
    return this._routerQuery.selectQueryParams(PRODUCT_MODE_QUERY_KEY)
      .pipe(map(param => param === PRODUCT_MODE_FAVOURITE));
  }

  selectTimelineResolution() {
    return this._routerQuery.selectQueryParams(TIMELINE_RESOLUTION_QUERY_KEY)
      .pipe(
        map(resolution => Number(resolution)),
        map(resolution => AVAILABLE_TIMELINE_RESOLUTIONS.includes(resolution) ? resolution : DEFAULT_TIMELINE_RESOLUTION)
      )
  }
}
