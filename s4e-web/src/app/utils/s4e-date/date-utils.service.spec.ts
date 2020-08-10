import {TestBed} from '@angular/core/testing';
import {DateUtilsService} from './date-utils.service';
import moment from 'moment';
import 'moment-timezone';

describe('DateUtilsService', () => {
  let service: DateUtilsService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DateUtilsService]
    });
    service = TestBed.get(DateUtilsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return null if feeded null', () => {
    expect(service.dateToApiString(null)).toBeNull();
  });
});
