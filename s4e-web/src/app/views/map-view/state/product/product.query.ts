/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Injectable} from '@angular/core';
import {EntityUIQuery, QueryEntity} from '@datorama/akita';
import {ProductStore} from './product.store';
import {
  AVAILABLE_TIMELINE_RESOLUTIONS,
  DEFAULT_TIMELINE_RESOLUTION,
  Product,
  PRODUCT_MODE_FAVOURITE,
  PRODUCT_MODE_QUERY_KEY,
  ProductState,
  ProductUIState,
  TIMELINE_RESOLUTION_QUERY_KEY,
  UIProductCategory
} from './product.model';
import {combineLatest, Observable} from 'rxjs';
import {IUILayer} from '../common.model';
import {map} from 'rxjs/operators';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {logIt} from '../../../../utils/rxjs/observable';

@Injectable({
  providedIn: 'root'
})
export class ProductQuery extends QueryEntity<ProductState, Product> {
  public readonly ui: EntityUIQuery<ProductUIState>;

  constructor(protected _store: ProductStore, private _routerQuery: RouterQuery) {
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
    const rankSort = (a: any, b: any) => (a.rank < b.rank ? -1 : 1);
    const orderCategories = (products: IUILayer[]) => {
      const nonUniqueCategories = products.reduce(
        (acc, product) => (acc = [...acc, product.category]),
        []
      );
      const uniqueCategories = Array.from(
        new Set(nonUniqueCategories.map(category => category.id))
      ).map(uniqueId =>
        nonUniqueCategories.find(category => category.id === uniqueId)
      );

      return uniqueCategories
        .sort(rankSort)
        .map(category => `${category.label}-${category.id}`);
    };
    const orderProducts = (products: IUILayer[]) => {
      const orderedCategories = orderCategories(products);
      let orderedProducts = orderedCategories.map(() => []);
      products.forEach(product =>
        orderedProducts[
          orderedCategories.indexOf(
            `${product.category.label}-${product.category.id}`
          )
        ].push(product)
      );
      orderedProducts = orderedProducts.map(products => products.sort(rankSort));
      return orderedProducts;
    };

    return this.selectAllFilteredAsUILayer().pipe(
      map(products =>
        orderProducts(products).filter(
          categoryProducts => !!categoryProducts && categoryProducts.length > 0
        )
      )
    );
  }

  public selectAllFilteredAsUILayer(): Observable<IUILayer[]> {
    return combineLatest(
      this._routerQuery.selectQueryParams(PRODUCT_MODE_QUERY_KEY),
      this.selectAllAsUILayer()
    ).pipe(
      map(([mode, layers]) => {
        switch (mode) {
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
    return combineLatest(
      this.selectAll(),
      this.ui.selectAll(),
      this.ui.select('collapsedCategories'),
      this.selectActiveId()
    ).pipe(
      map(([products, productsUi, collapsedCategories, activeId]) =>
        products.map((pt, i) => ({
          cid: pt.id,
          label: pt.displayName,
          active: pt.id === activeId,
          category: {
            ...pt.productCategory,
            collapsed: collapsedCategories.indexOf(pt.productCategory.id) !== -1
          },
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
    return this.select(state => state.ui.availableDays).pipe(
      map(dates => dates.map(date => new Date(date)))
    );
  }

  selectIsFavouriteMode(): Observable<boolean> {
    return this._routerQuery
      .selectQueryParams(PRODUCT_MODE_QUERY_KEY)
      .pipe(map(param => param === PRODUCT_MODE_FAVOURITE));
  }

  selectTimelineResolution() {
    return this._routerQuery.selectQueryParams(TIMELINE_RESOLUTION_QUERY_KEY).pipe(
      map(resolution => Number(resolution)),
      map(resolution =>
        AVAILABLE_TIMELINE_RESOLUTIONS.includes(resolution)
          ? resolution
          : DEFAULT_TIMELINE_RESOLUTION
      )
    );
  }
}
