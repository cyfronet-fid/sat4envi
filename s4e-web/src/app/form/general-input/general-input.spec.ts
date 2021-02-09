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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {Component} from '@angular/core';
import {ControlContainer} from '@angular/forms';
import {GeneralInput} from './general-input';

@Component({
  selector: 'mock-modal',
  template: '',
  styles: []
})
class MockGeneralInput extends GeneralInput {
  constructor(cc: ControlContainer) {
    super(cc, undefined);
  }
}

class MockExtFormDirective {
  disabled: boolean = false;
  labelSize: number = 4;
  isLoading: boolean = false;
}

class MockControlContainer {}

describe('GeneralInput', () => {
  let component: GeneralInput;
  let fixture: ComponentFixture<MockGeneralInput>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [MockGeneralInput],
        imports: [],
        providers: [{provide: ControlContainer, useClass: MockControlContainer}]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(MockGeneralInput);
    component = fixture.componentInstance;
    component.controlId = 'general';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
