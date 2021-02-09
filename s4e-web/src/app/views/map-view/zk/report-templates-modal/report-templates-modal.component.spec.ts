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
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {REPORT_TEMPLATES_MODAL_ID} from './report-templates-modal.model';
import {ReportTemplatesModalComponent} from './report-templates-modal.component';
import {makeLocalStorageTestingProvider} from '../../../../app.configuration.spec';

describe('ReportModalComponent', () => {
  let component: ReportTemplatesModalComponent;
  let fixture: ComponentFixture<ReportTemplatesModalComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
        providers: [
          {
            provide: MODAL_DEF,
            useValue: {
              id: REPORT_TEMPLATES_MODAL_ID,
              size: 'lg'
            }
          },
          makeLocalStorageTestingProvider({})
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportTemplatesModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
