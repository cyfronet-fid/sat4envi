import { TestBed } from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import { ConfigurationService } from './configuration.service';
import { ConfigurationStore } from './configuration.store';
import {ConfigurationQuery} from './configuration.query';
import {TestingConfigProvider} from '../../../../../app.configuration.spec';
import {take, toArray} from 'rxjs/operators';
import {MapModule} from '../../../map.module';
import {NotificationService} from 'notifications';

describe('ConfigurationService', () => {
  let service: ConfigurationService;
  let query: ConfigurationQuery;
  let store: ConfigurationStore;
  let http: HttpTestingController;
  let notifications: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TestingConfigProvider],
      imports: [ MapModule, HttpClientTestingModule ]
    });

    notifications = TestBed.get(NotificationService);
    query = TestBed.get(ConfigurationQuery);
    service = TestBed.get(ConfigurationService);
    store = TestBed.get(ConfigurationStore);
    http = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  describe('shareConfiguration', () => {
    const body = {
      thumbnail: '00',
      path: '/abc?a=1',
      emails: ['a@a'],
      description: 'test',
      caption: 'test'
    };

    it('should call http, pass data and return true if ok', async () => {
      spyOn(notifications, 'addGeneral').and.stub();
      const r = service.shareConfiguration(body).toPromise();
      const req = http.expectOne({method: 'POST', url: 'api/v1/share-link'});
      req.flush({});

      expect(req.request.body).toEqual(body);
      expect(await r).toBeTruthy();
      expect(await query.selectLoading().pipe(take(1)).toPromise()).toBeFalsy();
      expect(notifications.addGeneral).toHaveBeenCalledWith({"content": "Link został wysłany na adres a@a", "type": "success"});
    });

    it('should return set store error and return false if http errored', async () => {
      const loadings = query.selectLoading().pipe(take(3), toArray()).toPromise();
      const r = service.shareConfiguration(body).toPromise();
      const req = http.expectOne({method: 'POST', url: 'api/v1/share-link'});
      req.flush({}, {status: 500, statusText: 'server error'});

      expect(req.request.body).toEqual(body);
      expect(await r).toBeFalsy();
      expect((await query.selectError().pipe(take(1)).toPromise()).status).toEqual(500);
      expect(await loadings).toEqual([false, true, false]);
    });

    it('should set loading', async () => {
      const r = service.shareConfiguration(body);
      expect((await query.selectLoading().pipe(take(1)).toPromise())).toBeTruthy();
    });
  });
});
