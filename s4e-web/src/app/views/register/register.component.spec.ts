import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {RegisterComponent} from './register.component';
import {RouterTestingModule} from '@angular/router/testing';
import {ShareModule} from '../../common/share.module';
import {RegisterService} from './state/register.service';
import {FormErrorModule} from '../../components/form-error/form-error.module';
import {By} from '@angular/platform-browser';
import {RecaptchaFormsModule, RecaptchaModule} from 'ng-recaptcha';
import {TestingConfigProvider} from '../../app.configuration.spec';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let registerService: RegisterService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, ShareModule, FormErrorModule, RecaptchaModule, RecaptchaFormsModule],
      declarations: [RegisterComponent],
      providers: [TestingConfigProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    registerService = TestBed.get(RegisterService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should not send non valid form', () => {
    const spy = spyOn(registerService, 'register');
    component.register();
    expect(spy).not.toHaveBeenCalled();
  });

  it('should send valid form', () => {
    const spy = spyOn(registerService, 'register');
    component.form.setValue({email: 'user@domain', password: 'pass', passwordRepeat: 'pass', recaptcha: 'captcha'});
    component.register();
    expect(spy).toHaveBeenCalled();
  });

  it('should validate password', () => {
    component.form.controls.password.setValue('');
    expect(component.form.controls.password.valid).toBeFalsy();
    expect(component.form.controls.passwordRepeat.valid).toBeFalsy();


    component.form.controls.password.setValue('pass');
    expect(component.form.controls.password.valid).toBeTruthy();
    expect(component.form.controls.passwordRepeat.valid).toBeFalsy();

    component.form.controls.passwordRepeat.setValue('pass');
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
    fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement.click();
    expect(spy).toBeCalledWith();
  });

  it('should call RegisterService.register on submit', () => {
    component.form.controls.email.setValue('user@domain');
    component.form.controls.password.setValue('password1234');
    component.form.controls.passwordRepeat.setValue('password1234');
    component.form.controls.recaptcha.setValue('test-recaptcha');

    const spy = spyOn(TestBed.get(RegisterService), 'register').and.stub();

    component.register();

    expect(spy).toBeCalledWith('user@domain', 'password1234', 'test-recaptcha');
  });

  it('should not call RegisterService.register on submit if form is not valid', () => {
    component.form.controls.email.setValue('invalid');
    component.form.controls.password.setValue('password1234');
    component.form.controls.passwordRepeat.setValue('password1234');
    component.form.controls.recaptcha.setValue('recaptcha');

    const spy = spyOn(TestBed.get(RegisterService), 'register').and.stub();

    component.register();

    expect(spy).not.toBeCalledWith('invalid', 'password1234', 'recaptcha');
  });
});
