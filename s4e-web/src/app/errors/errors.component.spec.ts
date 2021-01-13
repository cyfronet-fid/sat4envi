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

import { By } from '@angular/platform-browser';
import { ParamMap } from '@angular/router/src/shared';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientModule } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { ErrorsModule } from './errors.module';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorsComponent } from './errors.component';
import { ReplaySubject, Observable, Subject } from 'rxjs';

class ActivatedRouteStub {
  paramMap: Subject<ParamMap> = new ReplaySubject(1);
  queryParamMap: Subject<ParamMap> = new ReplaySubject(1);

  constructor() {
    this.paramMap.next(convertToParamMap({}));
    this.queryParamMap.next(convertToParamMap({backLink: '/'}));
  }
}


describe('ErrorComponent', () => {
  let component: ErrorsComponent;
  let fixture: ComponentFixture<ErrorsComponent>;
  let route: ActivatedRouteStub;
  const backLink = '/back-link';

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ ErrorsModule, HttpClientModule, RouterTestingModule ],
      providers: [{provide: ActivatedRoute, useClass: ActivatedRouteStub}]
    })
    .compileComponents();
    route = TestBed.get(ActivatedRoute);
  }));

  beforeEach(() => {
    route.paramMap.next(convertToParamMap({errorCode: '404'}));
    route.queryParamMap.next(convertToParamMap({backLink}));
    fixture = TestBed.createComponent(ErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display 404 error', () => {
    expect(fixture.debugElement.query(By.css('.title')).nativeElement.textContent).toBe('Błąd 404: nie znaleziono poszukiwanej strony')
  })
});
