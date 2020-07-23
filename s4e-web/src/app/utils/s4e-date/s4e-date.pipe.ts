import {Inject, LOCALE_ID, Pipe} from '@angular/core';
import moment from 'moment';
import {DatePipe} from '@angular/common';
import environment from 'src/environments/environment';

@Pipe({
  name: 'S4EDate'
})
export class S4EDatePipe extends DatePipe {
  constructor(@Inject(LOCALE_ID) locale: string) {
    super(locale);
  }

  transform(value: any, args?: any): any {
    if (value == null) {
      return '';
    }
    return super.transform(moment.utc(value, environment.backendDateFormat).toDate());
  }

}
