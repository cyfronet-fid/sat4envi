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
import {UIDesignFormComponent} from './uidesign-form.component';
import {S4EFormsModule} from '../form.module';
import {BsLocaleService} from 'ngx-bootstrap/datepicker';
import {FontAwesomeTestingModule} from '@fortawesome/angular-fontawesome/testing';

describe('UIDesignFormComponent', () => {
  let component: UIDesignFormComponent;
  let fixture: ComponentFixture<UIDesignFormComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [S4EFormsModule, FontAwesomeTestingModule],
        providers: [BsLocaleService]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(UIDesignFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
