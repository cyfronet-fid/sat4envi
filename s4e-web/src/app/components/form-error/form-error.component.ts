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

import {Component, Input, OnInit} from '@angular/core';
import {ValidatorsModel} from '@ng-stack/forms';
import {AbstractControl} from '@angular/forms';

@Component({
  selector: 'ul[s4e-form-error]',
  templateUrl: './form-error.component.html',
  styleUrls: ['./form-error.component.scss'],
  host: {'[class]': '"invalid-feedback special__error"'}
})
export class FormErrorComponent implements OnInit {
  @Input() errors: ValidatorsModel | {server?: string[]};
  @Input() control: AbstractControl;

  constructor() { }

  ngOnInit() {
  }

  getServerErrorMessages() {
    const errors = !!this.control && this.control.errors || this.errors;
    const hasServerErrors = !!errors && !!errors.server;
    const singleServerError = hasServerErrors
      && typeof errors.server === 'string'
      && [errors.server];
    const serverErrors = hasServerErrors
      && errors.server instanceof Array
      && errors.server;

    return serverErrors || singleServerError || [];
  }

  getErrors() {
    return this.control != null ? this.control.errors : this.errors;
  }
}
