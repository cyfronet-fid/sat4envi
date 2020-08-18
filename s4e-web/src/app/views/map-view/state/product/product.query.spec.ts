import {ProductQuery} from './product.query';
import {ProductStore} from './product.store';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {ProductFactory} from './product.factory.spec';
import {take, toArray} from 'rxjs/operators';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {ReplaySubject} from 'rxjs';
import {Product, PRODUCT_MODE_FAVOURITE} from './product.model';

describe('ProductQuery', () => {
  let store: ProductStore;
  let query: ProductQuery;
  let routerQuery: RouterQuery;


  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, RouterTestingModule, HttpClientTestingModule]
    });

    query = TestBed.get(ProductQuery);
    store = TestBed.get(ProductStore);
    routerQuery = TestBed.get(RouterQuery);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('should selectFavourites', async () => {
    const product = ProductFactory.build();
    store.set([product]);
    expect(await query.selectFavourites().pipe(take(1)).toPromise())
      .toEqual([]);
    store.update(product.id, {favourite: true});
    expect(await query.selectFavourites().pipe(take(1)).toPromise())
      .toEqual([{...product, favourite: true}]);
  });

  it('should selectFavouritesCount', async () => {
    const product = ProductFactory.build();
    store.set([product]);
    expect(await query.selectFavouritesCount().pipe(take(1)).toPromise()).toBe(0);
    store.update(product.id, {favourite: true});
    expect(await query.selectFavouritesCount().pipe(take(1)).toPromise()).toBe(1);
  });

  it('should selectAllFilteredAsUILayer', async () => {
    const queryParams$ = new ReplaySubject(1);
    spyOn(routerQuery, 'selectQueryParams').and.returnValue(queryParams$);
    queryParams$.next(undefined);
    const products = ProductFactory.buildList(2);
    const favProduct = products[0];
    const nonFavProduct = products[1];
    store.set(products);

    const filtered = query.selectAllFilteredAsUILayer().pipe(take(4), toArray()).toPromise();

    queryParams$.next(PRODUCT_MODE_FAVOURITE);

    store.update(favProduct.id, {favourite: true});

    queryParams$.next('');

    const toUILayer = (product: Product, favourite: boolean = false) => {
      return {
          cid: product.id,
          label: product.displayName,
          active: false,
          favourite: favourite,
          isLoading: false,
          isFavouriteLoading: false
        };
    }

    expect(await filtered).toEqual([
      products.map(p => toUILayer(p)),
      [],
      [toUILayer(favProduct, true)],
      [toUILayer(favProduct, true), toUILayer(nonFavProduct)]
    ]);
  });

  it('should selectIsFavouriteMode', async () => {

    const queryParams$ = new ReplaySubject(1);
    spyOn(routerQuery, 'selectQueryParams').and.returnValue(queryParams$);

    queryParams$.next('')

    const modes = query.selectIsFavouriteMode().pipe(take(2), toArray()).toPromise();

    queryParams$.next(PRODUCT_MODE_FAVOURITE);

    expect(await modes).toEqual([false, true]);
  });
});
