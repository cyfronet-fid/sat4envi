import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { By } from '@angular/platform-browser';
import { InstitutionFactory } from './../state/institution/institution.factory.spec';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';

import { InstitutionProfileComponent } from './institution-profile.component';
import { InstitutionProfileModule } from './institution-profile.module';
import { convertToParamMap, ParamMap, ActivatedRoute, Router } from '@angular/router';
import { Subject, ReplaySubject, of } from 'rxjs';
import { DebugElement } from '@angular/core';
import { S4eConfig } from 'src/app/utils/initializer/config.service';
import { InstitutionService } from '../state/institution/institution.service';
import { InjectorModule } from 'src/app/common/injector.module';

describe('InstitutionProfileComponent', () => {
  let component: InstitutionProfileComponent;
  let fixture: ComponentFixture<InstitutionProfileComponent>;
  let institutionService: InstitutionService;
  let route: ActivatedRoute;
  let router: Router;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        InjectorModule,
        InstitutionProfileModule,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        S4eConfig
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstitutionProfileComponent);
    component = fixture.componentInstance;
    route = TestBed.get(ActivatedRoute);
    router = TestBed.get(Router);
    institutionService = TestBed.get(InstitutionService);
    de = fixture.debugElement;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('Should display institution on init', fakeAsync(() => {
    const institution = InstitutionFactory.build();
    const spyFindBy = spyOn(institutionService, 'findBy').and.returnValue(of(institution));
    router.navigate(
      [],
      {
        relativeTo: route,
        queryParams: { institution: institution.slug },
        queryParamsHandling: 'merge',
        skipLocationChange: true
      }
    );
    tick();
    fixture.detectChanges();

    expect(spyFindBy).toHaveBeenCalledWith(institution.slug);
    expect(component.activeInstitution).toEqual(institution);

    const image = de.query(By.css('.institution__logo img'));
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
