import {TestBed} from '@angular/core/testing';
import {DateUtilsService} from './date-utils.service';
import {S4eConfig} from '../initializer/config.service';
import {TestingConfigProvider} from '../../app.configuration.spec';
import moment from 'moment';
import 'moment-timezone';

describe('DateUtilsService', () => {
  let service: DateUtilsService;
  let constants: S4eConfig;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DateUtilsService, TestingConfigProvider]
    });
    service = TestBed.get(DateUtilsService);
    constants = TestBed.get(S4eConfig);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return null if feeded null', () => {
    expect(service.dateToApiString(null)).toBeNull();
  });
});
