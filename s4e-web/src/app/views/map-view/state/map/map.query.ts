import { Injectable } from '@angular/core';
import { Query } from '@datorama/akita';
import { MapStore } from './map.store';
import {MapState} from './map.model';

@Injectable({ providedIn: 'root' })
export class MapQuery extends Query<MapState> {
  // private granulesToDays(granules.json: Granule[]): Day[] {
  //   const days: Day[] = [];
  //   let currDay: Day;
  //   for (const granule of granules.json) {
  //     const day = format(granule.timestampDate, 'yyyy-MM-dd');
  //     if (currDay === undefined || currDay.label !== day) {
  //       currDay = {
  //         label: day,
  //         granules.json: []
  //       };
  //       days.push(currDay);
  //     }
  //     currDay.granules.json.push(granule);
  //   }
  //   return days;
  // }

  constructor(protected store: MapStore) {
    super(store);
  }

}
