import { Pipe, PipeTransform } from '@angular/core';
import {HashMap} from '@datorama/akita';

@Pipe({
  name: 'errorKeys'
})
export class ErrorKeysPipe implements PipeTransform {
  transform(value: HashMap<any>, args?: any): string[] {
    return Object.keys(value || {});
  }

}
