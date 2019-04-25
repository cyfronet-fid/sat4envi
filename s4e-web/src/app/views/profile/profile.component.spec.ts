import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import {ShareModule} from '../../common/share.module';
import {RouterTestingModule} from '@angular/router/testing';
import {TestingConstantsProvider} from '../../app.constants.spec';
import {SessionService} from '../../state/session/session.service';
import {ProfileComponent} from './profile.component';
import {SessionQuery} from '../../state/session/session.query';
import {BehaviorSubject, ReplaySubject} from 'rxjs';
import {SessionStore} from '../../state/session/session.store';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let sessionService: SessionService;
  let sessionQuery: SessionQuery;
  let sessionStore: SessionStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ShareModule, RouterTestingModule],
      declarations: [ ProfileComponent ],
      providers: [TestingConstantsProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    sessionService = TestBed.get(SessionService);
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
});
