import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetPasswordComponent } from './reset-password.component';
import {RouterTestingModule} from '@angular/router/testing';
import {ActivatedRoute, Router} from '@angular/router';
import {ReplaySubject} from 'rxjs';

class MockActivatedRoute {
  queryParams = new ReplaySubject();
}

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let activatedRoute: MockActivatedRoute;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [ ResetPasswordComponent ],
      providers: [{provide: ActivatedRoute, useClass: MockActivatedRoute}]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResetPasswordComponent);
    activatedRoute = TestBed.get(ActivatedRoute);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate away if not token is provided', () => {
    const spy = spyOn(TestBed.get(Router), 'navigate');
    activatedRoute.queryParams.next({});
    expect(spy).toHaveBeenCalledWith(['/'], {replaceUrl: true});
  });
});
