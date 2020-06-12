import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { InstitutionFactory } from './../institution/institution.factory.spec';
import { InstitutionsSearchResultsQuery } from './institutions-search-results.query';
import { InstitutionsSearchResultsStore } from './institutions-search-results.store';
import { InstitutionService } from '../institution/institution.service';
import { Subject, ReplaySubject, of } from 'rxjs';
import { ParamMap, convertToParamMap, ActivatedRoute, Data } from '@angular/router';
import { TestBed, async } from '@angular/core/testing';
import { S4eConfig } from 'src/app/utils/initializer/config.service';

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

  beforeEach(async (() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        {provide: ActivatedRoute, useClass: ActivatedRouteStub},
        S4eConfig
      ]
    });

    institutionService = TestBed.get(InstitutionService);
    query = new InstitutionsSearchResultsQuery(new InstitutionsSearchResultsStore(), institutionService);
    activatedRoute = <ActivatedRouteStub>TestBed.get(ActivatedRoute);
  }));

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('should get selected institution slug', () => {
    const institution = InstitutionFactory.build();
    activatedRoute.queryParamMap.next(convertToParamMap({ institution: institution.slug }));
    query.getSelectedInstitutionSlugBy$(activatedRoute as undefined)
      .subscribe(activeInstitutionSlug => expect(activeInstitutionSlug).toEqual(institution.slug));
  });

  it('should get selected institution', () => {
    const institution = InstitutionFactory.build();
    const institutionServiceSpy = spyOn(institutionService, 'findBy').and.returnValue(of(institution));
    activatedRoute.queryParamMap.next(convertToParamMap({ institution: institution.slug }));
    activatedRoute.data.next({ isEditMode: true });
    query.getSelectedInstitutionBy$(activatedRoute as undefined)
      .subscribe((activeInstitution) => {
        expect(activeInstitution).toEqual(institution);
        expect(institutionServiceSpy).toHaveBeenCalledWith(institution.slug);
      })
  });
});
