import { environment } from './../../../environments/environment';
import {Injectable} from '@angular/core';
import moment from 'moment';

@Injectable()
export class DateUtilsService {
  constructor() {
  }

  dateToApiString(value: Date|null): any {
    if (value == null) {
      return null;
    }
    return moment(value).format(environment.backendDateFormat);
  }
}
