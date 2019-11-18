import {Component, EventEmitter, Inject, Input, LOCALE_ID, OnInit, Output} from '@angular/core';
import {Product} from '../state/product/product.model';
import moment from 'moment';
import {S4eConfig} from '../../../utils/initializer/config.service';
import {BehaviorSubject, ReplaySubject} from 'rxjs';

export interface Day {
  label: string;
  products: Product[];

}

@Component({
  selector: 's4e-timeline',
  templateUrl: './timeline.component.html',
  styleUrls: ['./timeline.component.scss']
})
export class TimelineComponent {
  private loadedMonths: string[] = [];
  public s = new BehaviorSubject(false);

  public filterInactiveDays = (date: Date) => {
    this.loadAvailableDates.emit(moment(date).format('YYYY-MM'));
    return true;
  };

  @Input() public loading: boolean = true;

  currentDate: string = '';
  startAt = null;

  @Input('currentDate') set _currentDate(v: string) {
    this.currentDate = v;
    this.startAt = moment(v, 'YYYY-MM-DD');
  }
  @Input() activeProduct: Product|null = null;
  @Input() products: Product[] = [];
  // @Input() set products(products: Product[] | null) {
  //   this.days = [];
  //   let currDay: Day;
  //   for (const product of (products || [])) {
  //     const day = formatDate(product.timestamp, 'shortDate', this.LOCALE_ID);
  //     if (currDay === undefined || currDay.label !== day) {
  //       currDay = {label: day, products: []};
  //       this.days.push(currDay);
  //     }
  //     currDay.products.push(product);
  //   }
  // }

  @Output() dateSelected = new EventEmitter<string>();
  @Output() selectProduct = new EventEmitter<Product>();
  @Output() loadAvailableDates = new EventEmitter<string>();

  selectDate($event: {value: Date}) {
    this.dateSelected.emit(moment($event.value).utc().format(this.config.momentDateFormatShort))
  }

  // tslint:disable-next-line:no-shadowed-variable
  constructor(@Inject(LOCALE_ID) private LOCALE_ID: string, private config: S4eConfig) { }

  monthSelected($event: any) {
    console.log($event)
  }
}
