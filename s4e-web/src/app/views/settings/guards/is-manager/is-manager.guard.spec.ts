import {TestBed} from '@angular/core/testing';
import {IsManagerGuard} from './is-manager.guard';
import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {ProfileQuery} from '../../../../state/profile/profile.query';
import {ProfileStore} from '../../../../state/profile/profile.store';
import {of} from 'rxjs';

@Component({selector: 'neutral', template: ''})
class NeutralComponent {
}

@Component({selector: 'restricted', template: ''})
class RestrictedComponent {
}

describe('IsManagerGuard', () => {
  let router: Router;
  let query: ProfileQuery;
  let store: ProfileStore;
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NeutralComponent, RestrictedComponent],
      imports: [RouterTestingModule.withRoutes(
        [
          {path: 'settings/profile', component: NeutralComponent},
          {path: 'restricted', canActivate: [IsManagerGuard], component: RestrictedComponent}
        ]
      )],
      providers: [IsManagerGuard, ProfileQuery, ProfileStore],
    });
    router = TestBed.get(Router);
    store = TestBed.get(ProfileStore);
    query = TestBed.get(ProfileQuery);
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
