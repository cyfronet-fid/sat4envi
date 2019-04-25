import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterComponent } from './register.component';
import {RouterTestingModule} from '@angular/router/testing';
import {ShareModule} from '../../common/share.module';
import {RegisterService} from './state/register.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let registerService: RegisterService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, ShareModule],
      declarations: [ RegisterComponent ]
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
    component.form.setValue({login: 'user@domain', password: 'pass', passwordRepeat: 'pass'});
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
    component.form.controls.login.setValue('');
    expect(component.form.controls.login.valid).toBeFalsy();
    component.form.controls.login.setValue('user');
    expect(component.form.controls.login.valid).toBeFalsy();
    component.form.controls.login.setValue('user@domain');
    expect(component.form.controls.login.valid).toBeTruthy();
  });
});
