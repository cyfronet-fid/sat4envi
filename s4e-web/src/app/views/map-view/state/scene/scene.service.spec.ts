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

import {TestBed} from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import {SceneService} from './scene.service';
import {SceneStore} from './scene.store.service';
import {SceneQuery} from './scene.query';
import {take, toArray} from 'rxjs/operators';
import {SceneFactory, SceneResponseFactory} from './scene.factory.spec';
import {LegendFactory} from '../legend/legend.factory.spec';
import {LegendQuery} from '../legend/legend.query';
import {LegendStore} from '../legend/legend.store';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {ProductFactory} from '../product/product.factory.spec';
import environment from 'src/environments/environment';
import {timezone} from '../../../../utils/miscellaneous/date-utils';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {RouterTestingModule} from '@angular/router/testing';
import {MapModule} from '../../map.module';
import {
  artifactDownloadLink,
  createSentinelSearchResult
} from '../sentinel-search/sentinel-search.model';

describe('SceneService', () => {
  let sceneService: SceneService;
  let sceneStore: SceneStore;
  let sceneQuery: SceneQuery;
  let legendQuery: LegendQuery;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [MapModule, RouterTestingModule, HttpClientTestingModule]
    });

    http = TestBed.inject(HttpTestingController);
    sceneService = TestBed.inject(SceneService);
    sceneStore = TestBed.inject(SceneStore);
    sceneQuery = TestBed.inject(SceneQuery);
    legendQuery = TestBed.inject(LegendQuery);
  });

  it('should create', () => {
    expect(sceneService).toBeTruthy();
  });

  describe('setActive', () => {
    it("should set legend to Product's if Scene does not have one", () => {
      const legend = LegendFactory.build();
      const scene = SceneFactory.build();
      const product = ProductFactory.build({legend});
      const productStore: ProductStore = TestBed.inject(ProductStore);
      sceneStore.set([scene]);
      productStore.set([product]);
      productStore.setActive(product.id);
      sceneService.setActive(scene.id);
    });
  });

  describe('get', () => {
    it('loading should be set', done => {
      const productId = 1;
      const product = ProductFactory.build({id: productId});
      const dateF = '2019-10-01';
      const stream = sceneQuery.selectLoading();

      stream.pipe(take(2), toArray()).subscribe(data => {
        expect(data).toEqual([true, false]);
        done();
      });

      sceneService.get(product, dateF).subscribe();

      const urlParams = `date=${dateF}&timeZone=${timezone()}`;
      const url = `${environment.apiPrefixV1}/products/${product.id}/scenes?${urlParams}`;
      const request = http.expectOne(url);
      request.flush([]);
    });

    it('should call http endpoint', () => {
      const productId = 1;
      const product = ProductFactory.build({id: productId});
      const dateF = '2019-10-01';
      sceneService.get(product, dateF).subscribe();

      const urlParams = `date=${dateF}&timeZone=${timezone()}`;
      const url = `${environment.apiPrefixV1}/products/${product.id}/scenes?${urlParams}`;
      const request = http.expectOne(url);
    });

    it('should set state in store', done => {
      const dateF = '2019-10-01';
      const product = ProductFactory.build();
      const productId = product.id;
      const productStore: ProductStore = TestBed.inject(ProductStore);
      productStore.add(product);
      const scene = SceneResponseFactory.build();

      sceneQuery
        .selectAll()
        .pipe(take(2), toArray())
        .subscribe(data => {
          expect(data).toEqual([
            [],
            [{...createSentinelSearchResult(scene), layerName: product.layerName}]
          ]);
          done();
        });

      sceneService.get(product, dateF).subscribe();

      const urlParams = `date=${dateF}&timeZone=${timezone()}`;
      const url = `${environment.apiPrefixV1}/products/${product.id}/scenes?${urlParams}`;
      const request = http.expectOne(url);
      request.flush([scene]);
    });
  });
});
