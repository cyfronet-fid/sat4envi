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
  Component, ElementRef, forwardRef, Input, Optional, ViewChild,
  ViewEncapsulation,
  OnInit,
  OnDestroy
} from '@angular/core';
import {ControlContainer, NG_VALUE_ACCESSOR} from '@angular/forms';
import {GeneralInput} from '../general-input/general-input';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {ExtFormDirective} from '../form-directive/ext-form.directive';

@Component({
  selector: 'ext-textarea',
  templateUrl: './textarea.component.html',
  styleUrls: ['./textarea.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TextareaComponent),
      multi: true
    }
  ]
})
export class TextareaComponent extends GeneralInput {
  @Input('placeholder') _placeholder: string;
  @ViewChild('input') inputRef: ElementRef;

  constructor(cc: ControlContainer, @Optional() extForm: ExtFormDirective) {
    super(cc, extForm);
  }

  ngOnInit(): void {
    super.ngOnInit();
    this.fc.valueChanges
      .pipe(untilDestroyed(this))
      .subscribe(val => this.onChange(null, val));
  }

  focus(): void {
    this.inputRef.nativeElement.focus();
  }

  writeValue(value: any): void {
    if (value !== undefined) {
      this.currentValue = value;
      this.fc.setValue(value)
    }
  }

  protected onSetLabel(value: string) {
    if (!this._placeholder) {
      this._placeholder = value;
    }
  }
}
