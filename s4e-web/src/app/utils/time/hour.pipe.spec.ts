import {HourPipe} from './hour.pipe';
import {TestBed} from '@angular/core/testing';
import {UtilsModule} from '../utils.module';
import momentTz from 'moment-timezone';

describe('TimePipe', () => {
  let pipe: HourPipe;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [UtilsModule],
      providers: [HourPipe]
    });
    pipe = TestBed.get(HourPipe);
  });

  it('create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return hours', () => {
    momentTz.tz.setDefault('Europe/Warsaw')
    expect(pipe.transform('2020-09-09T02:56:49.140Z')).toBe('04:56');
  });

  it('should handle empty values', () => {
    expect(pipe.transform('')).toBe('00:00');
    expect(pipe.transform(null)).toBe('00:00');
  });
});
