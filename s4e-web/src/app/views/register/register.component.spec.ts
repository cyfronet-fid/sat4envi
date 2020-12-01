import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {RegisterComponent} from './register.component';
import {RouterTestingModule} from '@angular/router/testing';
import {ShareModule} from '../../common/share.module';
import {RegisterService} from './state/register.service';
import {FormErrorModule} from '../../components/form-error/form-error.module';
import {By} from '@angular/platform-browser';
import {RecaptchaFormsModule, RecaptchaModule} from 'ng-recaptcha';
import { RegisterFactory } from './register.factory.spec';
import { RemoteConfigurationTestingProvider } from 'src/app/app.configuration.spec';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let registerService: RegisterService;
  let activatedRoute: ActivatedRoute;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        ShareModule,
        FormErrorModule,
        RecaptchaModule,
        RecaptchaFormsModule
      ],
      declarations: [RegisterComponent],
      providers: [RemoteConfigurationTestingProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    registerService = TestBed.get(RegisterService);
    activatedRoute = TestBed.get(ActivatedRoute);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should not send non valid form', () => {
    const spy = spyOn(registerService, 'register');
    component.register();
    expect(spy).not.toHaveBeenCalled();
  });

  it('should validate password', () => {
    component.form.controls.password.setValue('');
    expect(component.form.controls.password.valid).toBeFalsy();
    expect(component.form.controls.passwordRepeat.valid).toBeFalsy();


    component.form.controls.password.setValue('pass1234');
    expect(component.form.controls.password.valid).toBeTruthy();
    expect(component.form.controls.passwordRepeat.valid).toBeFalsy();

    component.form.controls.passwordRepeat.setValue('pass1234');
    expect(component.form.controls.password.valid).toBeTruthy();
    expect(component.form.controls.passwordRepeat.valid).toBeTruthy();
  });

  it('should validate login', () => {
    component.form.controls.email.setValue('');
    expect(component.form.controls.email.valid).toBeFalsy();
    component.form.controls.email.setValue('user');
    expect(component.form.controls.email.valid).toBeFalsy();
    component.form.controls.email.setValue('user@domain');
    expect(component.form.controls.email.valid).toBeTruthy();
  });

  it('clicking submit button should call register', () => {
    const spy = spyOn(component, 'register');
    fixture.debugElement
      .query(By.css('button[type="submit"]'))
      .nativeElement
      .click();
    expect(spy).toBeCalledWith();
  });

  it('should call RegisterService.register on submit', fakeAsync(() => {
    const spy = spyOn(TestBed.get(RegisterService), 'register').and.stub();
    spyOn(activatedRoute, 'queryParams')
      .and.returnValue(of());

    const userRegister = RegisterFactory.build();
    component.form.setValue(userRegister);
    component.register();

    tick();

    const {recaptcha, passwordRepeat, ...request} = userRegister;
    expect(spy).toHaveBeenCalledWith(request, recaptcha, undefined);
  }));

  it('should not call RegisterService.register on submit if form is not valid', () => {
    const userRegister = RegisterFactory.build();
    userRegister.email = 'invalid';
    component.form.setValue(userRegister);
    component.register();

    const spy = spyOn(TestBed.get(RegisterService), 'register').and.stub();

    component.register();

    const {recaptcha, passwordRepeat, ...request} = userRegister;
    expect(spy).not.toHaveBeenCalledWith(request, recaptcha);
  });
});
