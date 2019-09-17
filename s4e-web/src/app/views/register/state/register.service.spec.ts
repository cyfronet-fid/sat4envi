import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {RegisterService} from './register.service';
import {RegisterStore} from './register.store';
import {RouterTestingModule} from '@angular/router/testing';
import {RegisterQuery} from './register.query';
import {Router} from '@angular/router';
import {TestingConfigProvider} from '../../../app.configuration.spec';

describe('RegisterService', () => {
  let registerService: RegisterService;
  let registerStore: RegisterStore;
  let http: HttpTestingController;
  let registerQuery: RegisterQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RegisterService, RegisterStore, RegisterQuery, TestingConfigProvider],
      imports: [HttpClientTestingModule, RouterTestingModule]
    });

    http = TestBed.get(HttpTestingController);
    registerService = TestBed.get(RegisterService);
    registerStore = TestBed.get(RegisterStore);
    registerQuery = TestBed.get(RegisterQuery);
  });

  it('should be created', () => {
    expect(registerService).toBeDefined();
  });

  describe('register', () => {
    const data = {
      email: 'email',
      password: 'password'
    };

    it('should correctly pass email, password and captcha', () => {
      registerService.register(data.email, data.password, 'captcha');
      const req = http.expectOne('api/v1/register?g-recaptcha-response=captcha');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(data);
      req.flush({});
      http.verify();
    });

    it('should redirect on success', fakeAsync(() => {
      const router = TestBed.get(Router);
      const spy = spyOn(router, 'navigate').and.stub();

      registerService.register(data.email, data.password, 'captcha');
      const req = http.expectOne('api/v1/register?g-recaptcha-response=captcha');
      req.flush({});

      tick(1000);
      http.verify();
      expect(spy).toBeCalledWith(['/']);
    }));

    it('should handle error 400', (done) => {
      registerService.register(data.email, data.password, 'captcha');
      const req = http.expectOne('api/v1/register?g-recaptcha-response=captcha');
      req.flush({email: ['Invalid email']}, {status: 400, statusText: 'Bad Request'});

      registerQuery.selectError().subscribe(error => {
        expect(error).toEqual({email: ['Invalid email']});
        done();
      });

      http.verify();
    });

    it('should handle other errors', (done) => {
      registerService.register(data.email, data.password, 'captcha');
      const req = http.expectOne('api/v1/register?g-recaptcha-response=captcha');
      req.flush('Server Failed', {status: 500, statusText: 'Server Error'});

      registerQuery.selectError().subscribe(error => {
        expect(error).toEqual({__general__: ['Server Failed']});
        done();
      });

      http.verify();
    });
  });
});
