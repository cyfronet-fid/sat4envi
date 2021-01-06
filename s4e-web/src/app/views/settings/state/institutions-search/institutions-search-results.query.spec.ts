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

import { InstitutionProfileComponent } from './../../intitution-profile/institution-profile.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { InstitutionFactory } from './../institution/institution.factory.spec';
import { InstitutionsSearchResultsQuery } from './institutions-search-results.query';
import { InstitutionsSearchResultsStore } from './institutions-search-results.store';
import { InstitutionService } from '../institution/institution.service';
import { Subject, ReplaySubject, of } from 'rxjs';
import { ParamMap, convertToParamMap, ActivatedRoute, Data, Router } from '@angular/router';
import { TestBed, async } from '@angular/core/testing';

class ActivatedRouteStub {
  queryParamMap: Subject<ParamMap> = new ReplaySubject(1);
  data: Subject<Data> = new ReplaySubject(1);

  constructor() {
    this.queryParamMap.next(convertToParamMap({}));
    this.data.next({ isEditMode: false });
  }
}

describe('InstitutionSearchResultsQuery', () => {
  let query: InstitutionsSearchResultsQuery;
  let activatedRoute: ActivatedRouteStub;
  let institutionService: InstitutionService;
  let router: Router;

  beforeEach(async (() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        {provide: ActivatedRoute, useClass: ActivatedRouteStub}
      ]
    });

    institutionService = TestBed.get(InstitutionService);
    router = TestBed.get(Router);
    query = new InstitutionsSearchResultsQuery(new InstitutionsSearchResultsStore(), institutionService);
    activatedRoute = <ActivatedRouteStub>TestBed.get(ActivatedRoute);
  }));

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('should get selected institution', () => {
    const institution = InstitutionFactory.build();
    const institutionServiceSpy = spyOn(institutionService, 'findBy').and.returnValue(of(institution));
    router.navigate(
      [],
      {
        relativeTo: activatedRoute as any,
        queryParams: { institution: institution.slug },
        queryParamsHandling: 'merge',
        skipLocationChange: true
      }
    );
    activatedRoute.queryParamMap.next(convertToParamMap({ institution: institution.slug }));
    query.selectActive$(activatedRoute as any)
      .subscribe((activeInstitution) => {
        expect(activeInstitution).toEqual(institution);
        expect(institutionServiceSpy).toHaveBeenCalledWith(institution.slug);
      });
  });
});
