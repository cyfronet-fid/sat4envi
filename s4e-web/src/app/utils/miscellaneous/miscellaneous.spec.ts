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

import {disableEnableForm, resizeImage} from './miscellaneous';
import {async, TestBed} from '@angular/core/testing';
import {InjectorModule} from '../../common/injector.module';
import {Injector} from '@angular/core';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {DOCUMENT} from '@angular/common';

export interface FS {
  login: string;
}

describe('disableEnableForm', () => {
  let form: FormGroup<FS>;

  beforeEach(() => {
    form = new FormGroup<FS>({
      login: new FormControl('')
    });
  });

  it('should disable', () => {
    disableEnableForm(true, form);
    expect(form.disabled).toBeTruthy();
  });

  it('should enable', () => {
    disableEnableForm(false, form);
    expect(form.disabled).toBeFalsy();
  });
});

describe('deserializeJsonResponse', function () {
  interface IData {
    time: Date;
    login: string;
  }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [InjectorModule],
      providers: [Injector],
    });
    TestBed.get(InjectorModule);
  }));
});
