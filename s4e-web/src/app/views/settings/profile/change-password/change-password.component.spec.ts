import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ProfileModule } from './../profile.module';
import { ProfileService } from 'src/app/state/profile/profile.service';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangePasswordComponent } from './change-password.component';

describe('ChangePasswordComponent', () => {
  let component: ChangePasswordComponent;
  let fixture: ComponentFixture<ChangePasswordComponent>;
  let profileService: ProfileService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ProfileModule,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    })
    .compileComponents();

    profileService = TestBed.get(ProfileService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChangePasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not send non valid form', () => {
    const spy = spyOn(profileService, 'resetPassword');
    component.submitPasswordChange();
    expect(spy).not.toHaveBeenCalled();
  });

  it('should send valid form', () => {
    const spy = spyOn(profileService, 'resetPassword').and.returnValue(of(1));
    const oldPassword = 'zkMember';
    const newPassword = 'ZKMEMBER';
    component.form
      .setValue({oldPassword, newPassword});
    component.submitPasswordChange();
    expect(spy).toHaveBeenCalledWith(oldPassword, newPassword);
  });

  it('should validate old password', () => {
    component.form.controls.oldPassword.setValue('');
    expect(component.form.controls.oldPassword.valid).toBeFalsy();
    component.form.controls.oldPassword.setValue('password');
    expect(component.form.controls.oldPassword.valid).toBeTruthy();
  });

  it('should validate new password', () => {
    component.form.controls.newPassword.setValue('');
    expect(component.form.controls.newPassword.valid).toBeFalsy();
    component.form.controls.newPassword.setValue('password');
    expect(component.form.controls.newPassword.valid).toBeTruthy();
  });
});
