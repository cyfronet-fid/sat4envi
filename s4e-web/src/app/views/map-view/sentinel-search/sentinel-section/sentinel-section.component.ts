import {Component, forwardRef, Input, OnDestroy, OnInit} from '@angular/core';
import {SentinelSection} from '../../state/sentinel-search/sentinel-search.metadata.model';
import {SentinelSearchService} from '../../state/sentinel-search/sentinel-search.service';
import {FormControl} from '@ng-stack/forms';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {SentinelSearchQuery} from '../../state/sentinel-search/sentinel-search.query';
import {Observable} from 'rxjs';
import {
  AbstractControl,
  ControlValueAccessor,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  Validator
} from '@angular/forms';
import {disableEnableForm, isEmptyObject} from '../../../../utils/miscellaneous/miscellaneous';
import {map} from 'rxjs/operators';

@Component({
  selector: 's4e-sentinel-section',
  templateUrl: './sentinel-section.component.html',
  styleUrls: ['./sentinel-section.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SentinelSectionComponent),
      multi: true
    },
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => SentinelSectionComponent),
      multi: true,
    }
  ]
})
export class SentinelSectionComponent implements OnInit, OnDestroy, ControlValueAccessor, Validator {
  @Input() set sentinel(sentinel: SentinelSection) {
    this._sentinel = sentinel;
    this.selectedFc.setValue(this._sentinelSearchQuery.isSentinelSelected(sentinel.name), {emitEvent: false});
  }

  get sentinel(): SentinelSection | null {
    return this._sentinel;
  }

  public form: FormControl = new FormControl({});
  public visible$: Observable<boolean>;
  public selectedFc = new FormControl<boolean>();
  protected _sentinel: SentinelSection|null = null;

  constructor(private _sentinelSearchService: SentinelSearchService,
              private _sentinelSearchQuery: SentinelSearchQuery) { }

  ngOnInit(): void {
    this.visible$ = this._sentinelSearchQuery.selectVisibleSentinels()
      .pipe(untilDestroyed(this), map(sentinels => sentinels.includes(this.sentinel.name)));

    this.visible$.subscribe(visible => {
      this.selectedFc.setValue(visible);
      this.propagateChange(visible ? this.form.value : {});
    });

    this.form.valueChanges.pipe(untilDestroyed(this)).subscribe(val => this.propagateChange(val));
  }

  ngOnDestroy(): void {}

  toggleVisibility() {
    if (this.isDisabled) {
      return;
    }
    this._sentinelSearchService.toggleSentinelVisibility(this.sentinel.name);
  }

  // ***************************************************************************
  // Validator methods and fields
  // ***************************************************************************
  validate(control: AbstractControl): ValidationErrors | null {
    return this.selectedFc.value ? this.form.errors : null;
  }

  // ***************************************************************************
  // ControlValueAccessor methods and fields
  // ***************************************************************************
  protected propagateChange: Function = (_: any) => {};
  protected onTouched: any = () => {};
  protected isDisabled: boolean = false;

  registerOnChange(fn: any): void {
    this.propagateChange = fn;
    this.propagateChange(this.selectedFc.value ? this.form.value : {});
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
    disableEnableForm(isDisabled, this.selectedFc);
    disableEnableForm(isDisabled, this.form);
  }

  writeValue(obj: any): void {
    if (!!obj && !isEmptyObject(obj)) {
      this.form.setValue(obj);
    }
  }
}
