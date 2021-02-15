/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Inject,
  Input,
  LOCALE_ID,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  Renderer2,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  merge,
  Observable,
  of,
  ReplaySubject,
  Subject
} from 'rxjs';
import {map, switchMap, tap} from 'rxjs/operators';
import {Scene, SceneWithUI} from '../state/scene/scene.model';
import {yyyymmdd} from '../../../utils/miscellaneous/date-utils';
import {AkitaGuidService} from '../state/search-results/guid.service';
import {DEFAULT_TIMELINE_RESOLUTION} from '../state/product/product.model';
import moment from 'moment';
import {distinctUntilChangedDE} from '../../../utils/rxjs/observable';
import {TimelineService} from '../state/scene/timeline.service';
import {SceneQuery} from '../state/scene/scene.query';
import {
  BsDatepickerDirective,
  DatepickerDateCustomClasses
} from 'ngx-bootstrap/datepicker';
import {BsDatePickerUtils} from './bs-date-picker-utils';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';
import {FormControl} from '@ng-stack/forms';

export interface Day {
  label: string;
  products: Scene[];
}

export interface DataPoint {
  points: SceneWithUI[];
  position: number;
  selected: boolean;
}

export const DATEPICKER_FORMAT_CUSTOMIZATION = {
  fullPickerInput: {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  },
  datePickerInput: {year: 'numeric', month: 'numeric', day: 'numeric'},
  timePickerInput: {hour: 'numeric', minute: 'numeric'},
  monthYearLabel: {year: 'numeric', month: 'short'},
  dateA11yLabel: {year: 'numeric', month: '2-digit', day: '2-digit'},
  monthYearA11yLabel: {year: 'numeric', month: 'long'}
};

export class PointStacker {
  constructor(
    private scenePointWidth: number,
    private scenePointMaximumSpace: number
  ) {}

  stack(scenes: SceneWithUI[], width: number, activeScene: Scene): DataPoint[] {
    const retScenes: DataPoint[] = [];

    for (let i = 0; i < scenes.length; ) {
      const scenePoint = [];
      let j = i;
      const startPosition = scenes[i].position * width * 0.01;
      let endPosition = startPosition;
      let selected = false;
      do {
        if (activeScene && activeScene.id === scenes[j].id) {
          selected = true;
        }
        scenePoint.push(scenes[j]);
        endPosition = scenes[j].position * width * 0.01;
        ++j;

        if (j === scenes.length) {
          break;
        }
      } while (
        scenes[j].position * width * 0.01 - startPosition <
        this.scenePointWidth + 2 * this.scenePointMaximumSpace
      );

      const position = (endPosition - startPosition) * 0.5 + startPosition;
      i = j;
      retScenes.push({
        points: scenePoint,
        position: (position * 100.0) / width,
        selected: selected
      });
    }

    return retScenes;
  }
}

@UntilDestroy()
@Component({
  selector: 's4e-timeline',
  templateUrl: './timeline.component.html',
  styleUrls: ['./timeline.component.scss'],
  providers: []
})
export class TimelineComponent
  implements OnInit, OnDestroy, OnChanges, AfterViewInit {
  static readonly HOURMARKS_COUNT = 6;
  @ViewChild('datepicker', {read: BsDatepickerDirective, static: true})
  datepicker: BsDatepickerDirective;
  @Input() loading: boolean = true;
  @Input() resolution: number = DEFAULT_TIMELINE_RESOLUTION;
  currentDate: string = '';
  public activeScene: Scene | null = null;
  @Input() startTime: string;
  hourmarks: string[] = [];
  public scenes$: Observable<DataPoint[]>;
  public activeStackedPoint: DataPoint | null = null;
  public isLive$ = this.sceneQuery
    .selectLoading()
    .pipe(map(() => this.sceneQuery.getValue().isLiveMode));
  @Output() dateSelected = new EventEmitter<string>();
  @Output() selectedScene = new EventEmitter<Scene>();
  @Output() previousScene = new EventEmitter<void>();
  @Output() nextScene = new EventEmitter<void>();
  @Output() nextDay = new EventEmitter<void>();
  @Output() previousDay = new EventEmitter<void>();
  @Output() increaseResolution = new EventEmitter<void>();
  @Output() decreaseResolution = new EventEmitter<void>();
  @Output() loadAvailableDates = new EventEmitter<string>();
  @Output() lastAvailableScene = new EventEmitter<void>();
  @Output() openSceneSelection = new EventEmitter<void>();
  pickerState: boolean = false;
  componentId: string;
  private pointStacker = new PointStacker(10, 15);
  private readonly _activeScene$: ReplaySubject<Scene | null> = new ReplaySubject(1);
  private readonly _timelineWidth$: ReplaySubject<number> = new ReplaySubject(1);
  private readonly _scenesWithUi$: ReplaySubject<SceneWithUI[]> = new ReplaySubject(
    1
  );
  // }
  private updateStream = new Subject<void>();

  private isDatepickerOpen$ = new BehaviorSubject(false);

  // tslint:disable-next-line:no-shadowed-variable
  constructor(
    @Inject(LOCALE_ID) private LOCALE_ID: string,
    private renderer: Renderer2,
    private guidService: AkitaGuidService,
    private element: ElementRef,
    private timelineService: TimelineService,
    private sceneQuery: SceneQuery
  ) {
    //class name can not start with number
    this.componentId = `D${this.guidService.guid()}`;
  }

  @Input('activeScene') set _activeScene(scene: Scene | null) {
    this.activeScene = scene;
    this._activeScene$.next(scene);
  }

  @Input('scenes') set scenesWithUI(scenes: SceneWithUI[]) {
    this._scenesWithUi$.next(scenes);
  }

  public dateClasses: DatepickerDateCustomClasses[] = [];

  @Input()
  public set datesEnabled(dates: Date[]) {
    this.dateClasses = dates.map(date => ({
      date,
      classes: ['calendar-data-available']
    }));
  }

  datepickerFc = new FormControl<Date>();

  @Input('currentDate') set _currentDate(v: string) {
    this.currentDate = v;
    this.datepickerFc.setValue(new Date(v), {emitEvent: false});
  }

  bsDatePickerUtils!: BsDatePickerUtils;

  ngAfterViewInit(): void {
    this.bsDatePickerUtils = new BsDatePickerUtils(this.datepicker);

    this.isDatepickerOpen$
      .pipe(
        switchMap(open =>
          open
            ? this.bsDatePickerUtils
                .monthChanged$()
                .pipe(tap(yearMonth => this.loadAvailableDates.emit(yearMonth)))
            : of(null)
        ),
        untilDestroyed(this)
      )
      .subscribe();
  }

  toggleLiveMode() {
    this.timelineService.toggleLiveMode();
  }

  async selectDate(date: Date) {
    const turnOfLiveMode = await this.timelineService.confirmTurningOfLiveMode();
    if (!turnOfLiveMode || date == null) {
      return;
    }

    this.dateSelected.emit(yyyymmdd(date));
  }

  monthSelected($event: any) {}

  async goToPreviousDay() {
    const turnOfLiveMode = await this.timelineService.confirmTurningOfLiveMode();
    if (!turnOfLiveMode) {
      return;
    }

    this.previousDay.emit();
  }

  async goToNextDay() {
    const turnOfLiveMode = await this.timelineService.confirmTurningOfLiveMode();
    if (!turnOfLiveMode) {
      return;
    }

    this.nextDay.emit();
  }

  async goToPreviousScene() {
    const turnOfLiveMode = await this.timelineService.confirmTurningOfLiveMode();
    if (!turnOfLiveMode) {
      return;
    }

    this.previousScene.emit();
  }

  async goToNextScene() {
    const turnOfLiveMode = await this.timelineService.confirmTurningOfLiveMode();
    if (!turnOfLiveMode) {
      return;
    }

    this.nextScene.emit();
  }

  async setPickerOpenState(open: boolean) {
    if (open) {
      const turnOfLiveMode = await this.timelineService.confirmTurningOfLiveMode();
      if (!turnOfLiveMode) {
        return;
      }
    }

    this.isDatepickerOpen$.next(open);
    this.pickerState = open;
  }

  async selectScene(scene: Scene) {
    const turnOfLiveMode = await this.timelineService.confirmTurningOfLiveMode();
    if (!turnOfLiveMode) {
      return;
    }

    this.selectedScene.emit(scene);
    this.activeStackedPoint = null;
  }

  async goToLastAvailableScene() {
    const turnOfLiveMode = await this.timelineService.confirmTurningOfLiveMode();
    if (!turnOfLiveMode) {
      return;
    }

    this.lastAvailableScene.emit();
  }

  ngOnInit(): void {
    this.scenes$ = this.aggregateScenes();

    this.onResize();
  }

  ngOnDestroy(): void {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.resolution || changes.startTime) {
      this.hourmarks = [];
      for (
        let i = 0;
        i < this.resolution &&
        this.hourmarks.length < TimelineComponent.HOURMARKS_COUNT;
        i += this.resolution / TimelineComponent.HOURMARKS_COUNT
      ) {
        let date = moment(this.startTime);
        date.add(i, 'hours');
        this.hourmarks.push(date.format('HH:mm'));
      }
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize(event?) {
    this._timelineWidth$.next(
      (this.element.nativeElement as HTMLElement).clientWidth
    );
  }

  private aggregateScenes(): Observable<DataPoint[]> {
    return merge(
      of<[SceneWithUI[], number, Scene]>([[], undefined, null]),
      combineLatest([
        this._scenesWithUi$.asObservable(),
        this._timelineWidth$.asObservable(),
        this._activeScene$.asObservable()
      ])
    ).pipe(
      distinctUntilChangedDE(),
      map(([scenes, width, activeScene]) =>
        this.pointStacker.stack(scenes, width, activeScene)
      )
    );
  }
}
