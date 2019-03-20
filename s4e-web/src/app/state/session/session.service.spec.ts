import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SessionService } from './session.service';
import { SessionStore } from './session.store';
import {RouterTestingModule} from '@angular/router/testing';
import {TestingConstantsProvider} from '../../app.constants.spec';

describe('SessionService', () => {
  let sessionService: SessionService;
  let sessionStore: SessionStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SessionService,
        SessionStore,
        HttpClientTestingModule,
        TestingConstantsProvider
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([])
      ]
    });

    sessionService = TestBed.get(SessionService);
    sessionStore = TestBed.get(SessionStore);
  });

  it('should be created', () => {
    expect(sessionService).toBeDefined();
  });

});
