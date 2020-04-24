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
