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
  Component,
  ElementRef,
  forwardRef,
  Input,
  Optional,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {ControlContainer, NG_VALUE_ACCESSOR, AbstractControl} from '@angular/forms';
import {GeneralInput} from '../general-input/general-input';
import {ExtFormDirective} from '../form-directive/ext-form.directive';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';

const SPECIAL_TO_REGULAR_TYPES = {
  date: 'text',
  percent: 'number'
};

@UntilDestroy()
@Component({
  selector: 'ext-input',
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true
    }
  ]
})
export class InputComponent extends GeneralInput {
  @Input() public inputType: string = 'text';
  public controlType: string = 'text';

  @Input() public css: string = '';
  @ViewChild('input', {static: true}) inputRef: ElementRef;

  focus(): void {
    this.inputRef.nativeElement.focus();
  }

  @Input('type')
  set type(type: string) {
    this.inputType = type;
    this.controlType =
      (!!SPECIAL_TO_REGULAR_TYPES[type] && SPECIAL_TO_REGULAR_TYPES[type]) || type;
  }

  @Input('placeholder') public _placeholder: string;

  constructor(
    cc: ControlContainer,
    @Optional() extForm: ExtFormDirective,
    private ref: ElementRef
  ) {
    super(cc, extForm);
  }

  ngOnInit(): void {
    super.ngOnInit();
    this.fc.valueChanges
      .pipe(untilDestroyed(this))
      .subscribe(val => this.onChange(null, val));
  }

  onChange(e: Event, value: any) {
    if (this.controlType === 'number') {
      return super.onChange(e, !value ? undefined : Number(value));
    }
    return super.onChange(e, value);
  }

  writeValue(value: any): void {
    if (!!value) {
      this.currentValue = value;
      this.fc.setValue(value);
    }
  }

  protected onSetLabel(value: string) {
    if (!this._placeholder) {
      this._placeholder = value;
    }
  }
}
