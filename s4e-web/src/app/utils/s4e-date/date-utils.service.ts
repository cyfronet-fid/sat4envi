import {Injectable} from '@angular/core';
import moment from 'moment';
import {S4eConfig} from '../initializer/config.service';

@Injectable()
export class DateUtilsService {
  constructor(private constants: S4eConfig) {
  }

  dateToApiString(value: Date|null): any {
    if (value == null) {
      return null;
    }
    return moment(value).format(this.constants.backendDateFormat);
  }
}
