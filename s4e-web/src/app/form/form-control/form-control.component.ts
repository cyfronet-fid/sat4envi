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

import {Component, Input, OnInit, Optional} from '@angular/core';
import {FormControl} from '@angular/forms';
import {ExtFormDirective} from '../form-directive/ext-form.directive';

export type VisualType = 'standard' | 'input-group' | 'blank';
export type ControlSize = 'md' | 'lg' | 'sm' | 'xs';

export interface IFormControlOptions {
  labelSize: number;
  formGroupClass: string;
  visualType: VisualType;
  size: ControlSize;
  tooltip: string;
  help: string;
  label: string;
  controlId?: string;
  required: boolean;
  control?: FormControl;
  errors: string[];
  showErrorMessages: boolean;
}

@Component({
  selector: 'ext-form-control',
  templateUrl: './form-control.component.html',
  styleUrls: ['./form-control.component.scss']
})
export class FormControlComponent implements OnInit {
  static readonly DEFAULT_VISUAL_TYPE = 'standard';
  static readonly DEFAULT_LABEL_SIZE = 3;
  _options: IFormControlOptions = {
    formGroupClass: 'form-group-sm',
    visualType: FormControlComponent.DEFAULT_VISUAL_TYPE,
    labelSize: undefined, //will inherit
    tooltip: '',
    help: '',
    label: 'Label',
    required: true,
    control: undefined,
    size: 'sm',
    errors: [],
    showErrorMessages: true
  };

  @Input('options') set options(newOptions: Partial<IFormControlOptions>) {
    this._options = {...this._options, ...newOptions} as IFormControlOptions;
  }
  constructor(@Optional() private extForm: ExtFormDirective) {}

  ngOnInit() {}

  isWarning() {}

  isSuccess() {}

  isError(): boolean {
    return (
      this._options.control &&
      this._options.control.invalid &&
      this._options.control.touched
    );
  }

  getErrors(): string[] {
    if (!this._options.control || this._options.control.untouched) {
      return [];
    }
    return this._options.errors.length > 0
      ? this._options.errors
      : (this._options.control.errors as string[]);
  }

  get visualType(): VisualType {
    return this._options.visualType || FormControlComponent.DEFAULT_VISUAL_TYPE;
  }

  public getLabelSize(): number {
    return (
      this._options.labelSize ||
      (this.extForm && this.extForm.labelSize) ||
      FormControlComponent.DEFAULT_LABEL_SIZE
    );
  }
}
