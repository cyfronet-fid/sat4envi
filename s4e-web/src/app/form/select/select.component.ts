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
  forwardRef,
  Host,
  Optional,
  SkipSelf,
  ViewChild,
  ViewEncapsulation,
  OnInit,
  OnDestroy
} from '@angular/core';
import {
  ControlContainer,
  FormControl,
  NG_VALUE_ACCESSOR,
  SelectControlValueAccessor
} from '@angular/forms';
import {GeneralInput} from '../general-input/general-input';
import {DateUtilsService} from '../../utils/s4e-date/date-utils.service';
import {ExtFormDirective} from '../form-directive/ext-form.directive';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 'ext-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true
    }
  ]
})
export class SelectComponent extends GeneralInput {
  inputFormControl: FormControl = new FormControl();
  @ViewChild(SelectControlValueAccessor, {static: true})
  select: SelectControlValueAccessor;

  constructor(
    private dateUtils: DateUtilsService,
    @Optional() extForm: ExtFormDirective,
    @Optional() @Host() @SkipSelf() cc: ControlContainer
  ) {
    super(cc, extForm);
  }

  ngOnInit() {
    this.inputFormControl.valueChanges
      .pipe(untilDestroyed(this))
      .subscribe(value => {
        this.onChange(null, value);
      });
    super.ngOnInit();
  }

  writeValue(value: any): void {
    this.currentValue = value;
    this.inputFormControl.setValue(value);
  }

  setDisabledState(isDisabled: boolean): void {
    super.setDisabledState(isDisabled);
    if (isDisabled) {
      this.inputFormControl.disable();
    } else {
      this.inputFormControl.enable();
    }
  }
}
