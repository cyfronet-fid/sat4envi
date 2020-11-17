import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LoginComponent} from './login.component';
import {ShareModule} from '../../common/share.module';
import {RouterTestingModule} from '@angular/router/testing';
import {SessionService} from '../../state/session/session.service';
import {FormErrorModule} from '../../components/form-error/form-error.module';
import {of} from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let sessionService: SessionService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ShareModule, RouterTestingModule, FormErrorModule],
      declarations: [LoginComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    sessionService = TestBed.get(SessionService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not send non valid form', () => {
    const spy = spyOn(sessionService, 'login$');
    component.login();
    expect(spy).not.toHaveBeenCalled();
  });

  it('should send valid form', () => {
    const spy = spyOn(sessionService, 'login$').and.returnValue(of());
    component.form.setValue({email: 'user@domain', password: 'pass'});
    component.login();
    expect(spy).toHaveBeenCalled();
  });

  it('should validate password', () => {
    component.form.controls.password.setValue('');
    expect(component.form.controls.password.valid).toBeFalsy();
    component.form.controls.password.setValue('pass');
    expect(component.form.controls.password.valid).toBeTruthy();
  });

  it('should validate email', () => {
    component.form.controls.email.setValue('');
    expect(component.form.controls.email.valid).toBeFalsy();
    component.form.controls.email.setValue('user');
    expect(component.form.controls.email.valid).toBeFalsy();
    component.form.controls.email.setValue('user@domain');
    expect(component.form.controls.email.valid).toBeTruthy();
  });
});
