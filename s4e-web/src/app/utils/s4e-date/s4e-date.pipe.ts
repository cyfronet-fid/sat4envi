import {Pipe} from '@angular/core';
import {S4eConfig} from '../initializer/config.service';
import moment from 'moment';
import {DatePipe} from '@angular/common';

@Pipe({
  name: 'S4EDate'
})
export class S4EDatePipe extends DatePipe {
  constructor(locale: string, private config: S4eConfig) {
    super(locale);
  }

  transform(value: any, args?: any): any {
    return super.transform(moment.utc(value, this.config.backendDateFormat).toDate());
  }

}
