import {TestBed} from '@angular/core/testing';
import {IsManagerGuard} from './is-manager.guard';
import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {of} from 'rxjs';
import {SessionQuery} from '../../../../state/session/session.query';
import {SessionStore} from '../../../../state/session/session.store';

@Component({selector: 'neutral', template: ''})
class NeutralComponent {
}

@Component({selector: 'restricted', template: ''})
class RestrictedComponent {
}

describe('IsManagerGuard', () => {
  let router: Router;
  let query: SessionQuery;
  let store: SessionStore;
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NeutralComponent, RestrictedComponent],
      imports: [RouterTestingModule.withRoutes(
        [
          {path: 'settings/profile', component: NeutralComponent},
          {path: 'restricted', canActivate: [IsManagerGuard], component: RestrictedComponent}
        ]
      )],
      providers: [IsManagerGuard, SessionQuery, SessionStore],
    });
    router = TestBed.get(Router);
    store = TestBed.get(SessionStore);
    query = TestBed.get(SessionQuery);
  });

  it('should create', () => {
    expect(TestBed.get(IsManagerGuard)).toBeTruthy();
  });

  it('should allow if selectCanSeeInstitutions resolves true', async () => {
    spyOn(query, 'selectCanSeeInstitutions').and.returnValue(of(true));
    expect(await router.navigate(['/settings/profile'])).toBeTruthy();
  });

  it('should return redirect if selectCanSeeInstitutions resolves false', async () => {
    spyOn(query, 'selectCanSeeInstitutions').and.returnValue(of(false));
    expect(await router.navigate(['/restricted'])).toBeFalsy();
    expect(await router.isActive('settings/profile', true));
  });
});
