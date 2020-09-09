import { Pipe, PipeTransform } from '@angular/core';
import moment from 'moment';

@Pipe({
  name: 'hour'
})
export class HourPipe implements PipeTransform {

  transform(date: string): any {
    if (!date) {
      return '00:00'
    }
    return moment(date).format('HH:mm');
  }

}
