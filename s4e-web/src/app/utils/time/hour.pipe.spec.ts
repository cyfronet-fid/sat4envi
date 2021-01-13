/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
