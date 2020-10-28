import {emailListValidator} from './email-list-validator.utils';
import {FormControl} from '@angular/forms';

describe('emailListValidator', () => {
  function validateValue(value: string) {
    const control = new FormControl(value);
    return emailListValidator(control)
  }

  it('should work', () => {
    expect(validateValue('notValid')).toEqual({emails: true});
    expect(validateValue('admin; admin2 ? admin@mail.pl')).toEqual({emails: true});
    expect(validateValue('admin, admin@mail.pl')).toEqual({emails: true});
    expect(validateValue('admin@mail.pl')).toEqual(null);
    expect(validateValue('admin@mail.pl, admin2@mail.pl')).toEqual(null);
    expect(validateValue('admin@mail.pl,admin2@mail.pl')).toEqual(null);
    expect(validateValue('admin@mail.pl,  admin2@mail.pl')).toEqual(null);
    expect(validateValue('   admin@mail.pl,  admin2@mail.pl   ')).toEqual(null);
    expect(validateValue('')).toEqual(null);
    expect(validateValue(null)).toEqual(null);
  });
});
