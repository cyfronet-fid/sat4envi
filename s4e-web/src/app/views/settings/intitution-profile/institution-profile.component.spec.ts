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

import { SessionService } from './../../../state/session/session.service';
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
import { InstitutionService } from '../state/institution/institution.service';

class ActivatedRouteStub {
  paramMap: Subject<ParamMap> = new ReplaySubject(1);
  queryParamMap: Subject<ParamMap> = new ReplaySubject(1);

  constructor() {
    this.paramMap.next(convertToParamMap({}));
    this.queryParamMap.next(convertToParamMap({backLink: '/'}));
  }
}

describe('InstitutionProfileComponent', () => {
  let component: InstitutionProfileComponent;
  let fixture: ComponentFixture<InstitutionProfileComponent>;
  let institutionService: InstitutionService;
  let route: ActivatedRouteStub;
  let router: Router;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        InstitutionProfileModule,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        {provide: ActivatedRoute, useClass: ActivatedRouteStub}
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstitutionProfileComponent);
    component = fixture.componentInstance;
    route = <ActivatedRouteStub>TestBed.get(ActivatedRoute);
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
    route.queryParamMap.next(convertToParamMap({ institution: institution.slug }));
    component.isManagerOfActive$ = of(true);
    tick();
    fixture.detectChanges();

    expect(spyFindBy).toHaveBeenCalledWith(institution.slug);

    const image = de.query(By.css('.institution__logo img'));
    expect(image).toBeTruthy();

    const title = de.query(By.css('#institution-title'));
    expect(title.nativeElement.innerHTML).toContain(institution.name);

    const address = de.query(By.css('#institution-address'));
    expect(address.nativeElement.innerHTML).toContain(institution.address);

    const postalCode = de.query(By.css('#institution-postal-code'));
    expect(postalCode.nativeElement.innerHTML).toContain(institution.postalCode);

    const phone = de.query(By.css('#institution-phone'));
    expect(phone.nativeElement.innerHTML.split(':')[1].trim()).toContain(institution.phone);

    const secondaryPhone = de.query(By.css('#institution-second-phone'));
    expect(secondaryPhone.nativeElement.innerHTML.split(':')[1].trim()).toContain(institution.secondaryPhone);

    const editInstitutionBtn = de.query(By.css('[data-ut="edit-institution"]'));
    expect(editInstitutionBtn).toBeTruthy();

    const addChildBtn = de.query(By.css('[data-ut="add-child-btn"]'));
    expect(addChildBtn).toBeTruthy();

    const goToProfileBtn = de.query(By.css('[data-ut="go-to-user-profile"]'));
    expect(goToProfileBtn).toBeFalsy();

    const institutionChildren = de.query(By.css('[data-ut="institution-children"]'));
    expect(institutionChildren).toBeTruthy();
  }));

  it('Should display only institution details when user is only member', fakeAsync(() => {
    const institution = InstitutionFactory.build();
    const spyFindBy = spyOn(institutionService, 'findBy').and.returnValue(of(institution));
    component.isManagerOfActive$ = of(false);
    route.queryParamMap.next(convertToParamMap({ institution: institution.slug }));
    tick();
    fixture.detectChanges();

    expect(spyFindBy).toHaveBeenCalledWith(institution.slug);

    const image = de.query(By.css('.institution__logo img'));
    expect(image).toBeTruthy();

    const title = de.query(By.css('#institution-title'));
    expect(title.nativeElement.innerHTML).toContain(institution.name);

    const address = de.query(By.css('#institution-address'));
    expect(address.nativeElement.innerHTML).toContain(institution.address);

    const postalCode = de.query(By.css('#institution-postal-code'));
    expect(postalCode.nativeElement.innerHTML).toContain(institution.postalCode);

    const phone = de.query(By.css('#institution-phone'));
    expect(phone.nativeElement.innerHTML.split(':')[1].trim()).toContain(institution.phone);

    const secondaryPhone = de.query(By.css('#institution-second-phone'));
    expect(secondaryPhone.nativeElement.innerHTML.split(':')[1].trim()).toContain(institution.secondaryPhone);

    const editInstitutionBtn = de.query(By.css('[data-ut="edit-institution"]'));
    expect(editInstitutionBtn).toBeFalsy();

    const addChildBtn = de.query(By.css('[data-ut="add-child-btn"]'));
    expect(addChildBtn).toBeFalsy();

    const goToProfileBtn = de.query(By.css('[data-ut="go-to-user-profile"]'));
    expect(goToProfileBtn).toBeTruthy();

    const institutionChildren = de.query(By.css('[data-ut="institution-children"]'));
    expect(institutionChildren).toBeFalsy();
  }));
});
