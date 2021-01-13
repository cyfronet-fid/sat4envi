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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {InputComponent} from './input.component';
import {ControlContainer} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {S4EFormsModule} from '../form.module';

class MockedControlContainer {
  hasError(errorCode: string, path?: string[]): boolean {
    return false;
  }

  getError(errorCode: string, path?: string[]): any {
    return {};
  }
}

describe('InputComponent', () => {
  let component: InputComponent;
  let fixture: ComponentFixture<InputComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [S4EFormsModule],
      providers: [{provide: ControlContainer, useClass: MockedControlContainer}]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InputComponent);
    component = fixture.componentInstance;
    component.controlId = 'input';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  async function setComponentTypeInputAndExpect(type: string, input: string, expected: any) {
    let spy = jest.fn();
    const element = fixture.debugElement.query(By.css('input')).nativeElement;
    component.type = type;
    component.registerOnChange(spy);

    element.value = input;
    element.dispatchEvent(new Event('input'));

    fixture.detectChanges();
    await fixture.whenStable();

    expect(spy).toHaveBeenCalledWith(expected);
  }

  it('value accessor should return number if type="number"', async () => {
    await setComponentTypeInputAndExpect('number', '21', 21);
  });

  it('value accessor should return text if type="text"', async () => {
    await setComponentTypeInputAndExpect('text', '21', '21');
  });
});
