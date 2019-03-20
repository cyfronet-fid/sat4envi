import {Component, EventEmitter, Inject, Input, LOCALE_ID, OnInit, Output} from '@angular/core';
import {Granule} from '../state/granule/granule.model';
import {formatDate} from '@angular/common';

export interface Day {
  label: string;
  granules: Granule[];

}

@Component({
  selector: 's4e-timeline',
  templateUrl: './timeline.component.html',
  styleUrls: ['./timeline.component.scss']
})
export class TimelineComponent {

  public days: Day[] = [];

  @Input() public loading: boolean = true;

  @Input() set granules(granules: Granule[] | null) {
    this.days = [];
    let currDay: Day;
    for (const granule of (granules || [])) {
      const day = formatDate(granule.timestamp, 'shortDate', this.LOCALE_ID);
      if (currDay === undefined || currDay.label !== day) {
        currDay = {label: day, granules: []};
        this.days.push(currDay);
      }
      currDay.granules.push(granule);
    }
  }

  @Output() public selectGranule = new EventEmitter<Granule>();

  // tslint:disable-next-line:no-shadowed-variable
  constructor(@Inject(LOCALE_ID) private LOCALE_ID: string) { }
}
