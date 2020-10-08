import {ProductStore} from './product.store';
import {TestBed} from '@angular/core/testing';
import {LocalStorageTestingProvider, makeLocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {LocalStorage} from '../../../../app.providers';
import {COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY} from './product.model';
import {ProductQuery} from './product.query';

describe('ProductStore', () => {
  let store: ProductStore;
  let storage: Storage;
  let query: ProductQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [makeLocalStorageTestingProvider({[COLLAPSED_CATEGORIES_LOCAL_STORAGE_KEY]: JSON.stringify([1])})],
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    storage = TestBed.get(LocalStorage);
    query = TestBed.get(ProductQuery);
    store = TestBed.get(ProductStore);
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

  it('should initialize with localstorage', () => {
    expect(query.ui.getValue().collapsedCategories).toEqual([1])
  });
});
