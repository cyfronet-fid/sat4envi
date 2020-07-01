import {convertSentinelParam2FormControl} from './sentinel-search.metadata.model';
import {FormControl} from '@angular/forms';

describe('convertSentinelParam2FormControl', () => {
  it('should work form select', () => {
    const formControlDef = {
      queryParam: 'satellitePlatform',
      type: 'select',
      values: [
        'Sentinel-1A',
        'Sentinel-1B'
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
    const formControlDef = {queryParam: 'sensingFrom', type: 'datetime'};
    expect(convertSentinelParam2FormControl(formControlDef)).toBeInstanceOf(FormControl);
  });

  it('should work form text', () => {
    const formControlDef = {
      queryParam: 'relativeOrbitNumber',
      type: 'text'
    };

    expect(convertSentinelParam2FormControl(formControlDef)).toBeInstanceOf(FormControl);
  });
});
