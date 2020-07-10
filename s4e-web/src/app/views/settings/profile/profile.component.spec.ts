import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {SessionService} from '../../../state/session/session.service';
import {ProfileComponent} from './profile.component';
import {SessionQuery} from '../../../state/session/session.query';
import {SessionStore} from '../../../state/session/session.store';
import {TestingConfigProvider} from '../../../app.configuration.spec';
import {ProfileModule} from './profile.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let sessionService: SessionService;
  let sessionQuery: SessionQuery;
  let sessionStore: SessionStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ProfileModule, HttpClientTestingModule, RouterTestingModule],
      providers: [TestingConfigProvider]
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
    sessionStore.update(state => ({...state, email: null}));
    fixture.detectChanges();
    expect(fixture.debugElement.nativeElement.textContent).toContain('Zaloguj się by zobaczyć swój profil');
  });

  it('should show user email if he\'s logged in', () => {
    sessionStore.update(state => ({...state, email: 'email@domain'}));
    fixture.detectChanges();
    expect(fixture.debugElement.nativeElement.textContent).toContain('Email: email@domain');
  });
});
