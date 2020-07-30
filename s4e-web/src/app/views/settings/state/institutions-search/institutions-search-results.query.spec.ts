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
import { S4eConfig } from 'src/app/utils/initializer/config.service';
import { InjectorModule } from 'src/app/common/injector.module';

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
        InjectorModule,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        {provide: ActivatedRoute, useClass: ActivatedRouteStub},
        S4eConfig
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
