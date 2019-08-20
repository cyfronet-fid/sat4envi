import {ActivateStore} from '../../activate/state/activate.store';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {TestingConstantsProvider} from '../../../app.constants.spec';
import {RouterTestingModule} from '@angular/router/testing';
import {Router} from '@angular/router';
import {ActivateQuery} from './activate.query';
import {ActivateService} from './activate.service';
import {take, toArray} from 'rxjs/operators';

describe('ActivateService', () => {
  let activateService: ActivateService;
  let activateStore: ActivateStore;
  let http: HttpTestingController;
  let activateQuery: ActivateQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ActivateService, ActivateStore, ActivateQuery, TestingConstantsProvider],
      imports: [HttpClientTestingModule, RouterTestingModule]
    });

    http = TestBed.get(HttpTestingController);
    activateService = TestBed.get(ActivateService);
    activateStore = TestBed.get(ActivateStore);
    activateQuery = TestBed.get(ActivateQuery);
  });

  it('should be created', () => {
    expect(activateService).toBeDefined();
  });

  describe('activate', () => {
    const token = '4aaeb75b-73a9-4e8c-9dfc-02012fa8e2f4';

    it('should correctly pass token on mail confirmation', () => {
      activateService.activate(token);
      const req = http.expectOne({method: 'POST', url: `api/v1/confirm-email?token=${token}`});
      expect(req.request.body).toEqual({});
      expect(activateQuery.getValue().state).toEqual('activating');
      req.flush({});
      http.verify();
    });

    it('should redirect on successful mail confirmation', fakeAsync(() => {
      const router = TestBed.get(Router);
      const spy = spyOn(router, 'navigate').and.stub();

      activateService.activate(token);
      expect(activateQuery.getValue().state).toEqual('activating');


      const req = http.expectOne({method: 'POST', url: `api/v1/confirm-email?token=${token}`});
      req.flush({});

      tick(1000);
      http.verify();

      expect(spy).toBeCalledWith(['/login']);
      expect(activateQuery.getValue().state).toEqual('activating');
      activateQuery.selectError().pipe(take(2), toArray()).subscribe(error => {
        expect(error[1]).toBeNull();
      });
    }));

    it('should handle error on mail confirmation', (done) => {
      activateService.activate(token);
      const req = http.expectOne({method: 'POST', url: `api/v1/confirm-email?token=${token}`});
      req.flush({}, {status: 401, statusText: 'Bad Request'});

      expect(activateQuery.getValue().state).toEqual('activating');
      activateQuery.selectError().subscribe(error => {
        expect(error.status).toEqual(401);
        done();
      });
      http.verify();
    });

    it('should correctly pass token on resend', () => {
      activateService.resendToken(token);
      const req = http.expectOne({method: 'POST', url: `api/v1/resend-registration-token-by-token?token=${token}`});
      req.flush({});

      expect(req.request.body).toEqual({});
      expect(activateQuery.getValue().state).toEqual('resending');
      activateQuery.selectError().pipe(take(2), toArray()).subscribe(error => {
        expect(error[1]).toBeNull();
      });

      http.verify();
    });

    it('should do nothing successful resend', fakeAsync((done) => {
      const router = TestBed.get(Router);
      const spy = spyOn(router, 'navigate').and.stub();

      activateService.resendToken(token);
      const req = http.expectOne({method: 'POST', url: `api/v1/resend-registration-token-by-token?token=${token}`});
      req.flush({});

      tick(1000);
      http.verify();
      expect(spy).not.toBeCalled();
      expect(activateQuery.getValue().state).toEqual('resending');
      activateQuery.selectError().pipe(take(2), toArray()).subscribe(error => {
        expect(error[1]).toBeNull();
      });
    }));

    it('should handle error on token resend', (done) => {
      activateService.resendToken(token);
      const req = http.expectOne({method: 'POST', url: `api/v1/resend-registration-token-by-token?token=${token}`});
      req.flush({}, {status: 404, statusText: 'Bad Request'});

      expect(activateQuery.getValue().state).toEqual('resending');
      activateQuery.selectError().subscribe(error => {
        expect(error.status).toEqual(404);
        done();
      });
      http.verify();
    });
  });
});
