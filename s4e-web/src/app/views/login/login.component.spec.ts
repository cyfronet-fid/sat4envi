import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginComponent } from './login.component';
import {ShareModule} from '../../common/share.module';
import {RouterTestingModule} from '@angular/router/testing';
import {TestingConstantsProvider} from '../../app.constants.spec';
import {SessionService} from '../../state/session/session.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let sessionService: SessionService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ShareModule, RouterTestingModule],
      declarations: [ LoginComponent ],
      providers: [TestingConstantsProvider]
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
      const spy = spyOn(sessionService, 'login');
      component.login();
      expect(spy).not.toHaveBeenCalled();
  });

  it('should send valid form', () => {
    const spy = spyOn(sessionService, 'login');
    component.form.setValue({rememberMe: false, login: 'user@domain', password: 'pass'});
    component.login();
    expect(spy).toHaveBeenCalled();
  });

  it('should validate password', () => {
    component.form.controls.password.setValue('');
    expect(component.form.controls.password.valid).toBeFalsy();
    component.form.controls.password.setValue('pass');
    expect(component.form.controls.password.valid).toBeTruthy();
  });

  it('should validate login', () => {
    component.form.controls.login.setValue('');
    expect(component.form.controls.login.valid).toBeFalsy();
    component.form.controls.login.setValue('user');
    expect(component.form.controls.login.valid).toBeFalsy();
    component.form.controls.login.setValue('user@domain');
    expect(component.form.controls.login.valid).toBeTruthy();
  });
});
