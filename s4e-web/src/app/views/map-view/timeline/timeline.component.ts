import {
  Component,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  LOCALE_ID,
  OnDestroy,
  OnInit,
  Output,
  Renderer2,
  ViewChild
} from '@angular/core';
import {S4eConfig} from '../../../utils/initializer/config.service';
import {Subject} from 'rxjs';
import {OWL_DATE_TIME_FORMATS} from 'ng-pick-datetime';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {debounceTime} from 'rxjs/operators';
import {Scene} from '../state/scene/scene.model';
import {yyyymm, yyyymmdd} from '../../../utils/miscellaneous/date-utils';
import { AkitaGuidService } from '../state/search-results/guid.service';

export interface Day {
  label: string;
  products: Scene[];

}

export const DATEPICKER_FORMAT_CUSTOMIZATION = {
  fullPickerInput: {year: 'numeric', month: 'numeric', day: 'numeric', hour: 'numeric', minute: 'numeric'},
  datePickerInput: {year: 'numeric', month: 'numeric', day: 'numeric'},
  timePickerInput: {hour: 'numeric', minute: 'numeric'},
  monthYearLabel: {year: 'numeric', month: 'short'},
  dateA11yLabel: {year: 'numeric', month: '2-digit', day: '2-digit'},
  monthYearA11yLabel: {year: 'numeric', month: 'long'},
};

@Component({
  selector: 's4e-timeline',
  templateUrl: './timeline.component.html',
  styleUrls: ['./timeline.component.scss'],
  providers: [
    {provide: OWL_DATE_TIME_FORMATS, useValue: DATEPICKER_FORMAT_CUSTOMIZATION}
  ]
})
export class TimelineComponent implements OnInit, OnDestroy {
  @ViewChild('datepicker', {read: ElementRef}) datepicker: ElementRef;
  @Input() public loading: boolean = true;
  currentDate: string = '';
  startAt = null;
  @Input() activeScene: Scene | null = null;
  @Input() scenes: Scene[] = [];
  @Output() dateSelected = new EventEmitter<string>();
  @Output() selectedScene = new EventEmitter<Scene>();
  @Output() loadAvailableDates = new EventEmitter<string>();
  pickerState: boolean = false;
  componentId: string;
  // @Input() set products(products: Scene[] | null) {
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
  private updateStream = new Subject<void>();

  // tslint:disable-next-line:no-shadowed-variable
  constructor(@Inject(LOCALE_ID) private LOCALE_ID: string, private config: S4eConfig,
              private renderer: Renderer2, private guidService: AkitaGuidService) {
    //class name can not start with number
    this.componentId = `D${this.guidService.guid()}`;
  }

  private _availableDates: string[] = [];

  @Input()
  public set availableDates(value: string[]) {
    this._availableDates = value;
    if (this.pickerState == true) {
      this.hackCalendar();
    }
  }

  @Input('currentDate') set _currentDate(v: string) {
    this.currentDate = v;
    this.startAt = v;
  }

  public filterInactiveDays = (date: Date) => {
    this.loadAvailableDates.emit(yyyymm(date));
    this.updateStream.next();
    return true;
  };

  selectDate($event: { value: Date }) {
    this.dateSelected.emit(yyyymmdd($event.value));
  }

  monthSelected($event: any) {}

  setPickerOpenState(state: boolean) {
    this.pickerState = state;
    if (this.pickerState) {
      this.hackCalendar();
    }
  }

  hackCalendar() {
    this._availableDates
      .map(date => date.split('-').reverse().join('.'))
      .map(date => document.querySelector(`.${this.componentId} .owl-dt-calendar-cell[aria-label='${date}'] .owl-dt-calendar-cell-content`))
      .filter(element => element != null)
      .forEach(element => this.renderer.addClass(element, 'calendar-data-available'));
  }

  ngOnInit(): void {
    this.updateStream
      .pipe(untilDestroyed(this), debounceTime(50))
      .subscribe(() => this.hackCalendar());
  }

  ngOnDestroy(): void {}
}
