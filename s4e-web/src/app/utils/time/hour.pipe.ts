import { Pipe, PipeTransform } from '@angular/core';
import moment from 'moment';

@Pipe({
  name: 'hour'
})
export class HourPipe implements PipeTransform {

  transform(date: string, seconds: boolean = false): any {
    if (!date) {
      return seconds ? '00:00:00' : '00:00'
    }
    return moment(date).format(seconds ? 'HH:mm:ss' : 'HH:mm');
  }

}
