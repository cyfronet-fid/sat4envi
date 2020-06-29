import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'groupArray'
})
export class GroupArrayPipe implements PipeTransform {
  transform(value: any[], groupSize: number): any {
    const out = [];
    let group = [];

    value.forEach((element, i, array) => {
      group.push(element);

      if(((i + 1) % groupSize === 0 && i > 0) || i === array.length - 1) {
        out.push(group);
        group = [];
      }
    });

    return out;
  }
}
