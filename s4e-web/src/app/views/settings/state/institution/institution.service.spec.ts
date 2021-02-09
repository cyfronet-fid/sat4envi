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

import {AkitaGuidService} from '../../../map-view/state/search-results/guid.service';
import {InstitutionFactory} from './institution.factory.spec';
import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {InstitutionService} from './institution.service';
import {InstitutionStore} from './institution.store';
import {RouterTestingModule} from '@angular/router/testing';
import {of} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import environment from 'src/environments/environment';

describe('InstitutionService', () => {
  let institutionService: InstitutionService;
  let guidService: AkitaGuidService;
  let institutionStore: InstitutionStore;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InstitutionService, InstitutionStore, AkitaGuidService],
      imports: [HttpClientTestingModule, RouterTestingModule]
    });

    institutionService = TestBed.inject(InstitutionService);
    guidService = TestBed.inject(AkitaGuidService);
    institutionStore = TestBed.inject(InstitutionStore);
    http = TestBed.inject(HttpClient);
  });

  it('should be created', () => {
    expect(institutionService).toBeDefined();
  });

  it('should find institution', () => {
    const institution = InstitutionFactory.build();
    const spyHttp = spyOn(http, 'get').and.returnValue(of(institution));
    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}`;

    institutionService.findBy(institution.slug).subscribe(loadedInstitution => {
      expect(loadedInstitution).toEqual(institution);
      expect(spyHttp).toBeCalledWith(url);
    });
  });

  it('should store institutions with ids and depth', fakeAsync(() => {
    const institution = InstitutionFactory.build();
    const id = 'test-uid';
    const spyHttp = spyOn(http, 'get').and.returnValue(of([institution]));
    const spyGuidService = spyOn(guidService, 'guid').and.returnValue(id);
    const spyStore = spyOn(institutionStore, 'set');
    const url = `${environment.apiPrefixV1}/institutions`;

    institutionService.get();
    tick();

    expect(spyGuidService).toHaveBeenCalled();
    expect(spyStore).toHaveBeenCalledWith([{...institution, id, ancestorDepth: 0}]);
    expect(spyHttp).toHaveBeenCalledWith(url);
  }));

  it('should store calculated depths', fakeAsync(() => {
    const institutions = [
      InstitutionFactory.build({slug: 'test-1'}),
      InstitutionFactory.build({slug: 'test-2', parentSlug: 'test-1'}),
      InstitutionFactory.build({slug: 'test-10'}),
      InstitutionFactory.build({slug: 'test-3', parentSlug: 'test-2'})
    ];
    const id = 'test-id';
    const spyHttp = spyOn(http, 'get').and.returnValue(of(institutions));
    const url = `${environment.apiPrefixV1}/institutions`;
    const spyGuidService = spyOn(guidService, 'guid').and.returnValue(id);
    const spyStore = spyOn(institutionStore, 'set');

    institutionService.get();
    tick();
    expect(spyStore).toHaveBeenCalledWith([
      {...institutions[0], id, ancestorDepth: 0},
      {...institutions[1], id, ancestorDepth: 1},
      {...institutions[3], id, ancestorDepth: 2},
      {...institutions[2], id, ancestorDepth: 0}
    ]);
  }));

  it('should add new institution', fakeAsync(() => {
    const institution = InstitutionFactory.build();
    const url = `${environment.apiPrefixV1}/institutions/${institution.parentSlug}/child`;
    const spyHttp = spyOn(http, 'post').and.returnValue(of(null));

    institutionService.createInstitutionChild$(institution);
    tick();

    expect(spyHttp).toHaveBeenCalledWith(url, institution);
  }));

  it('should update institution', fakeAsync(() => {
    const institution = InstitutionFactory.build();
    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}`;
    const spyHttp = spyOn(http, 'put').and.returnValue(of(null));

    institutionService.updateInstitution$(institution);
    tick();

    expect(spyHttp).toHaveBeenCalledWith(url, institution);
  }));
});
