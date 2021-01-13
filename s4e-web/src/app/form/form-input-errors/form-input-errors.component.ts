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
import {HashMap} from '@datorama/akita';

@Component({
  selector: 'ext-form-input-errors',
  templateUrl: './form-input-errors.component.html',
  styleUrls: ['./form-input-errors.component.scss']
})
export class FormInputErrorsComponent implements OnInit {

  @Input() set errorList(value: HashMap<any>) {
    this.errors = Object.entries(value).map(([key, value]) => ({key, value}));
  }

  public errors: {key: string, value: any}[];

  constructor() { }

  ngOnInit() {
  }
}
