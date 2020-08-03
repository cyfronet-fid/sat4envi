import { TestBed } from '@angular/core/testing';
import {IsLoggedIn, IsNotLoggedIn} from './auth-guard.service';
import {RouterTestingModule} from '@angular/router/testing';
import {SessionQuery} from '../../state/session/session.query';
import {Component} from '@angular/core';
import {Router} from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

@Component({selector: 'neutral', template: ''})
class NeutralComponent {}

@Component({selector: 'sub', template: ''})
class SubComponent {}

@Component({selector: 'login', template: ''})
class LoginComponent {}

describe('IsLoggedIn & IsNotLoggedIn', () => {
  let router: Router;
  let query: SessionQuery;
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SubComponent, LoginComponent, NeutralComponent],
      imports: [
        RouterTestingModule.withRoutes(
          [
            {path: 'map/products', component: NeutralComponent},
            {path: 'login', canActivate: [IsNotLoggedIn], component: LoginComponent},
            {path: 'sub', canActivate: [IsLoggedIn], component: SubComponent}
          ]
        ),
        HttpClientTestingModule
      ],
      providers: [IsLoggedIn, IsNotLoggedIn, SessionQuery],
    });
    router = TestBed.get(Router);
    query = TestBed.get(SessionQuery);
  });
  describe('IsLoggedIn', () => {
    it('should create', () => {
      expect(TestBed.get(IsLoggedIn)).toBeTruthy();
    });

    it('should let in if user is logged in', async () => {
      spyOn(query, 'isLoggedIn').and.returnValue(true);
      expect(await router.navigate(['/sub'])).toBeTruthy();
    });

    it('should redirect to root if user is not logged in', async () => {
      spyOn(query, 'isLoggedIn').and.returnValue(false);
      expect(await router.navigate(['/sub'])).toBeFalsy();
      expect(router.isActive('/login', true)).toBeTruthy();
    });
  });

  describe('IsNotLoggedIn', () => {
    it('should create', () => {
      expect(TestBed.get(IsNotLoggedIn)).toBeTruthy();
    });

    it('should redirect to root if user is logged in', async () => {
      spyOn(query, 'isLoggedIn').and.returnValue(true);
      expect(await router.navigate(['/login'])).toBeFalsy();
      expect(router.isActive('/map/products', true)).toBeTruthy();
    });

    it('should let user if he/she is not logged in', async () => {
      spyOn(query, 'isLoggedIn').and.returnValue(false);
      expect(await router.navigate(['/login'])).toBeTruthy();
    });
  });
});
