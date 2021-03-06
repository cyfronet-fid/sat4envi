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

import {
  convertSentinelParam2FormControl,
  SentinelParam
} from './sentinel-search.metadata.model';
import {FormControl} from '@angular/forms';

describe('convertSentinelParam2FormControl', () => {
  it('should work form select', () => {
    const formControlDef: SentinelParam = {
      queryParam: 'satellitePlatform',
      label: 'Satellite Platform',
      type: 'select',
      values: [
        {value: 'Sentinel-1A', label: 'Sentinel 1A'},
        {value: 'Sentinel-1B', label: 'Sentinel1B'}
      ]
    };

    const fc = convertSentinelParam2FormControl(formControlDef);

    expect(fc).toBeInstanceOf(FormControl);
    fc.setValue(null);
    expect(fc.valid).toBeTruthy();
    fc.setValue('Sentinel-1A');
    expect(fc.valid).toBeTruthy();
  });

  it('should work form float', () => {
    const formControlDef = {
      queryParam: 'cloudCover',
      label: 'Cloud Cover',
      type: 'float',
      min: 0,
      max: 100
    };

    const fc = convertSentinelParam2FormControl(formControlDef);

    expect(fc).toBeInstanceOf(FormControl);
    fc.setValue(-1);
    expect(fc.invalid).toBeTruthy();
    fc.setValue(0);
    expect(fc.valid).toBeTruthy();
    fc.setValue(101);
    expect(fc.invalid).toBeTruthy();
  });

  it('should work form datetime', () => {
    const formControlDef = {
      queryParam: 'sensingFrom',
      type: 'datetime',
      label: 'Sensing From'
    };
    expect(convertSentinelParam2FormControl(formControlDef)).toBeInstanceOf(
      FormControl
    );
  });

  it('should work form text', () => {
    const formControlDef = {
      queryParam: 'relativeOrbitNumber',
      type: 'text',
      label: 'Relative Orbit Number'
    };

    expect(convertSentinelParam2FormControl(formControlDef)).toBeInstanceOf(
      FormControl
    );
  });
});
