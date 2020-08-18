import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'isEmpty'
})
export class IsEmptyPipe implements PipeTransform {
  transform(value: any, args?: any): any {
    return (value && (Object.keys(value).length === 0));
  }

}
