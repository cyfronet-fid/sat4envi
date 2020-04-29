import { ProfileService } from './../../../state/profile/profile.service';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ShareModule} from '../../../common/share.module';
import {RouterTestingModule} from '@angular/router/testing';
import {SessionService} from '../../../state/session/session.service';
import {ProfileComponent} from './profile.component';
import {SessionQuery} from '../../../state/session/session.query';
import {SessionStore} from '../../../state/session/session.store';
import {TestingConfigProvider} from '../../../app.configuration.spec';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let sessionService: SessionService;
  let sessionQuery: SessionQuery;
  let sessionStore: SessionStore;
  let profileService: ProfileService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ShareModule, RouterTestingModule],
      declarations: [ProfileComponent],
      providers: [TestingConfigProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    sessionService = TestBed.get(SessionService);
    profileService = TestBed.get(ProfileService);
    sessionQuery = TestBed.get(SessionQuery);
    sessionStore = TestBed.get(SessionStore);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should tell user to log in if it isnt', () => {
    sessionStore.update(state => ({...state, initialized: true, email: null, token: null}));
    fixture.detectChanges();
    expect(fixture.debugElement.nativeElement.textContent).toContain('Zaloguj się by zobaczyć swój profil');
  });

  it('should show user email if he\'s logged in', () => {
    sessionStore.update(state => ({...state, initialized: true, email: 'email@domain', token: '12345'}));
    fixture.detectChanges();
    expect(fixture.debugElement.nativeElement.textContent).toContain('Email: email@domain');
  });

  it('should not send non valid form', () => {
    const spy = spyOn(profileService, 'resetPassword');
    component.submitPasswordChange();
    expect(spy).not.toHaveBeenCalled();
  });

  it('should send valid form', () => {
    const spy = spyOn(profileService, 'resetPassword');
    component.form
      .setValue({oldPassword: 'zkMember', newPassword: 'ZKMEMBER'});
    component.submitPasswordChange();
    expect(spy).toHaveBeenCalled();
  });

  it('should validate old password', () => {
    component.form.controls.oldPassword.setValue('');
    expect(component.form.controls.oldPassword.valid).toBeFalsy();
    component.form.controls.oldPassword.setValue('pass');
    expect(component.form.controls.oldPassword.valid).toBeTruthy();
  });

  it('should validate new password', () => {
    component.form.controls.newPassword.setValue('');
    expect(component.form.controls.newPassword.valid).toBeFalsy();
    component.form.controls.newPassword.setValue('pass');
    expect(component.form.controls.newPassword.valid).toBeTruthy();
  });
});
