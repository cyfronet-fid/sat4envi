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

import { untilDestroyed } from 'ngx-take-until-destroy';
import {
  Component, EventEmitter, forwardRef, Host, Input, Optional, Output, SkipSelf,
  ViewEncapsulation,
  OnDestroy,
  OnInit
} from '@angular/core';
import {ControlContainer, FormControl, NG_VALUE_ACCESSOR} from '@angular/forms';
import {GeneralInput} from '../general-input/general-input';
import {DateUtilsService} from '../../utils/s4e-date/date-utils.service';
import {ExtFormDirective} from '../form-directive/ext-form.directive';
import {disableEnableForm} from '../../utils/miscellaneous/miscellaneous';

@Component({
  selector: 'ext-datepicker',
  templateUrl: './datepicker.component.html',
  styleUrls: ['./datepicker.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DatepickerComponent),
      multi: true
    }
  ]
})
export class DatepickerComponent extends GeneralInput {
  _placeholder: string = 'RRRR-MM-DD';
  inputFormControl: FormControl = new FormControl();

  constructor(private dateUtils: DateUtilsService,
              @Optional() extForm: ExtFormDirective,
              @Optional() @Host() @SkipSelf() cc: ControlContainer) {
    super(cc, extForm);
  }

  @Input() placement: 'top'|'bottom'|'left'|'right' = 'bottom';
  @Input() showNowButton: boolean = false;
  @Input() showExtraButton: boolean = true;
  @Input() extraButtonIcon: string = 'question-sign';

  @Output() extraButtonClick = new EventEmitter();

  ngOnInit() {
    super.ngOnInit();
    this.inputFormControl.valueChanges
      .pipe(untilDestroyed(this))
      .subscribe(value => {
        this.onChange(null, this.dateUtils.dateToApiString(value));
      });
  }

  setDateToNow() {
    this.writeValue(new Date());
  }

  writeValue(value: any): void {
    this.currentValue = value;
    this.inputFormControl.setValue(value);
  }


  setDisabledState(isDisabled: boolean) {
    super.setDisabledState(isDisabled);
    disableEnableForm(isDisabled, this.inputFormControl);
  }
}
