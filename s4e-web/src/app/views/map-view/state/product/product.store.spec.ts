/*
 * Copyright 2020 ACC Cyfronet AGH
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
