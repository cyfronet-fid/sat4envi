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

import {Component, forwardRef, Input, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {ControlValueAccessor, FormArray, NG_VALUE_ACCESSOR} from '@angular/forms';
import {CheckBoxEntry} from './check-box-list.model';
import {FormControl} from '@ng-stack/forms';
import {untilDestroyed} from 'ngx-take-until-destroy';

@Component({
  selector: 's4e-check-box-list',
  templateUrl: './check-box-list.component.html',
  styleUrls: ['./check-box-list.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CheckBoxListComponent),
      multi: true
    }
  ]
})
export class CheckBoxListComponent<T> implements OnInit, OnDestroy, ControlValueAccessor {
  form: FormArray = new FormArray([]);
  private _items: CheckBoxEntry<T>[] = [];

  get items(): CheckBoxEntry<T>[] {
    return this._items;
  }

  @Input() set items(val: CheckBoxEntry<T>[]) {
    this._items = val;
    while (this.form.length !== 0) {
      this.form.removeAt(0);
    }
    this._items.forEach(item => this.form.push(new FormControl<boolean>(false)))
  }
  protected propagateChange: Function = (_: any) => {};
  protected onTouched: any = () => {};
  constructor() { }

  ngOnInit() {
    this.form.valueChanges.pipe(untilDestroyed(this)).subscribe((values: boolean[]) => {
      this.propagateChange({_: values.map((v, i) => this.items[i].value).filter((v, i) => values[i])});
    });
  }

  ngOnDestroy(): void {}

  writeValue(obj: any): void {
    if (obj == null) {obj = {_: []};}
    if(obj._ == null || !Array.isArray(obj._)) {
      throw new Error(`CheckBoxListComponent requires array as a value, value is: ${obj._}`)
    }
    obj = obj._;
    this.items.forEach((item, i) => {
      this.form.at(i).setValue(obj.indexOf(item.value) != -1);
    });
  }

  registerOnChange(fn: any): void {
    this.propagateChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    if (isDisabled) {
      this.form.disable();
    } else {
      this.form.enable();
    }
  }
}
