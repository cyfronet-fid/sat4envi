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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormErrorComponent } from './form-error.component';
import {FormErrorModule} from './form-error.module';
import {FormControl} from '@ng-stack/forms';
import {By} from '@angular/platform-browser';

describe('FormErrorComponent', () => {
  let component: FormErrorComponent;
  let fixture: ComponentFixture<FormErrorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormErrorModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FormErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not display if control was not touched', () => {
    const control = new FormControl<string>('');
    control.setErrors({'server': ['This password is too short']});
    component.control = control;
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('li')).length).toBe(0);
  });

  it('should display server messages correctly', () => {
    const control = new FormControl<string>('');
    const errorMessage = 'This password is too short';
    control.setErrors({'server': [errorMessage]});
    control.markAsTouched();
    component.control = control;
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('li'))[0].nativeElement.textContent).toContain(errorMessage);
  });
});
