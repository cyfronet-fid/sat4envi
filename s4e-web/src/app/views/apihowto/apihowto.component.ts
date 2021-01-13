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

import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {LOCATION} from '../../app.providers';
import environment from '../../../environments/environment';

@Component({
  templateUrl: './apihowto.component.html',
  styleUrls: ['./apihowto.component.scss']
})

export class ApihowtoComponent implements OnInit, OnDestroy {
  public readonly API_BASE: string;
  public readonly API_WMS: string;

  constructor(@Inject(LOCATION) location: Location) {
    this.API_BASE = location.origin + '/' + environment.apiPrefixV1;
    this.API_WMS = location.origin + '/wms';
  }

  ngOnInit() {
  }

  ngOnDestroy() {
  }
}
