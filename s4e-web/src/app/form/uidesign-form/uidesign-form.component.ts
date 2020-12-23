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

import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import environment from 'src/environments/environment';

@Component({
  selector: 's4e-uidesign-form',
  templateUrl: './uidesign-form.component.html',
  styleUrls: ['./uidesign-form.component.scss']
})
export class UIDesignFormComponent implements OnInit {
  customForm = new FormGroup({
    input: new FormControl(''),
    inputDisabled: new FormControl(''),
    select: new FormControl(1),
    textarea: new FormControl(''),
    datepicker: new FormControl(null),
    checkbox: new FormControl(true),
  });

  constructor() {
  }

  ngOnInit() {
    setTimeout(() => {
      this.customForm.controls.input.setErrors({
        required: true,
        email: true,
        mustMatch: true,
        server: ['Error #1', 'Error #2'],
        [environment.generalErrorKey]: 'General Type Error'
      });
      this.customForm.controls.input.markAsTouched();
      this.customForm.controls.inputDisabled.disable();
    }, 50);
  }
}
