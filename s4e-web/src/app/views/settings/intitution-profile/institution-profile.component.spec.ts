import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { By } from '@angular/platform-browser';
import { InstitutionFactory } from './../state/institution/institution.factory.spec';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';

import { InstitutionProfileComponent } from './institution-profile.component';
import { InstitutionProfileModule } from './institution-profile.module';
import { convertToParamMap, ParamMap, ActivatedRoute } from '@angular/router';
import { Subject, ReplaySubject, of } from 'rxjs';
import { DebugElement } from '@angular/core';
import { S4eConfig } from 'src/app/utils/initializer/config.service';
import { InstitutionService } from '../state/institution/institution.service';

class ActivatedRouteStub {
  queryParamMap: Subject<ParamMap> = new ReplaySubject(1);
  snapshot = {};

  constructor() {
    this.queryParamMap.next(convertToParamMap({}));
  }
}

describe('InstitutionProfileComponent', () => {
  let component: InstitutionProfileComponent;
  let fixture: ComponentFixture<InstitutionProfileComponent>;
  let activatedRoute: ActivatedRouteStub;
  let institutionService: InstitutionService;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        InstitutionProfileModule,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        {provide: ActivatedRoute, useClass: ActivatedRouteStub},
        S4eConfig
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InstitutionProfileComponent);
    component = fixture.componentInstance;
    activatedRoute = <ActivatedRouteStub>TestBed.get(ActivatedRoute);
    institutionService = TestBed.get(InstitutionService);
    de = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('Should display institution on init', fakeAsync(() => {
    const institution = InstitutionFactory.build();
    const spyFindBy = spyOn(institutionService, 'findBy').and.returnValue(of(institution));
    activatedRoute.queryParamMap.next(convertToParamMap({ institution: institution.slug }));
    tick();
    fixture.detectChanges();

    expect(spyFindBy).toHaveBeenCalledWith(institution.slug);
    expect(component.activeInstitution).toEqual(institution);

    const image = de.query(By.css('.profile__image img'));
    expect(image).toBeTruthy();

    const title = de.query(By.css('#institution-title'));
    expect(title.nativeElement.innerHTML).toContain(institution.name);

    const address = de.query(By.css('#institution-address'));
    expect(address.nativeElement.innerHTML).toContain(institution.address);

    const postalCode = de.query(By.css('#institution-postal-code'));
    expect(postalCode.nativeElement.innerHTML).toContain(institution.postalCode);

    const institutionAdminEmail = de.query(By.css('#institution-email'));
    expect(institutionAdminEmail.nativeElement.innerHTML).toContain('email: ' + institution.institutionAdminEmail);

    const phone = de.query(By.css('#institution-phone'));
    expect(phone.nativeElement.innerHTML.split(':')[1].trim()).toContain(institution.phone);

    const secondaryPhone = de.query(By.css('#institution-second-phone'));
    expect(secondaryPhone.nativeElement.innerHTML.split(':')[1].trim()).toContain(institution.secondaryPhone);
  }));
});
