/*
 * Copyright 2020 ACC Cyfronet AGH
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

import {Component, forwardRef, Input, OnDestroy, OnInit} from '@angular/core';
import {
  convertSentinelParam2FormControl,
  isSentinelFloatParam, isSentinelSelectParam,
  SentinelParam,
  SentinelSelectParam
} from '../../state/sentinel-search/sentinel-search.metadata.model';
import {
  AbstractControl,
  ControlValueAccessor,
  FormControl,
  FormGroup, NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  Validator,
  Validators
} from '@angular/forms';
import {disableEnableForm, stripEmpty} from '../../../../utils/miscellaneous/miscellaneous';
import {Subscription} from 'rxjs';

@Component({
  selector: 's4e-sentinel-form',
  templateUrl: './sentinel-form.component.html',
  styleUrls: ['./sentinel-form.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SentinelFormComponent),
      multi: true
    },
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => SentinelFormComponent),
      multi: true,
    }
  ],
})
export class SentinelFormComponent implements ControlValueAccessor, Validator {
  @Input('paramsDef') set paramsDef(sentinelParams: SentinelParam[]) {
    if(this.valueChangeSub) {
      this.valueChangeSub.unsubscribe();
    }
    this._paramsDef = sentinelParams;
    this.form = new FormGroup({});

    this._paramsDef.forEach((fcDef) => this.form.addControl(fcDef.queryParam, convertSentinelParam2FormControl(fcDef)));

    disableEnableForm(this.isDisabled, this.form);
    this.valueChangeSub = this.form.valueChanges.subscribe(val => this.onChange(val));
    this.onChange(this.form.value);
  }

  get paramsDef(): SentinelParam[] {
    return this._paramsDef;
  }

  public form: FormGroup = new FormGroup({});
  private _paramsDef: SentinelParam[] = [];
  private valueChangeSub: Subscription|null = null;

  constructor() { }

  selectOptions(def: SentinelParam): string|null[] {
    return (def as SentinelSelectParam).values
  }

  floatTooltip(def: SentinelParam) {
    if (isSentinelFloatParam(def) && def.max != null && def.min != null) {
      return `od ${def.min} do ${def.max}`;
    }
    return null;
  }

  // ***************************************************************************
  // Validator methods and fields
  // ***************************************************************************
  validate(control: AbstractControl): ValidationErrors | null {
    return this.form.valid ? null : {__general__: ['Niepoprawne dane wyszukiwania']};
  }

  // ***************************************************************************
  // ControlValueAccessor methods and fields
  // ***************************************************************************
  protected propagateChange: Function = (_: any) => {};
  protected onTouched: any = () => {};
  protected isDisabled: boolean = false;

  onChange(val) {
    this.propagateChange(stripEmpty(val));
  }

  registerOnChange(fn: any): void {
    this.propagateChange = fn;
    this.onChange(this.form.value);
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
    disableEnableForm(isDisabled, this.form);
  }

  writeValue(obj: any): void {
    if (!!obj) {
      this.form.patchValue(obj);
    }
  }
}
