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

import {ExpertHelpService} from '../state/expert-help.service';
import {EXPERT_HELP_MODAL_ID} from './expert-help-modal.model';
import {RouterTestingModule} from '@angular/router/testing';
import {MapModule} from '../../map.module';
import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
  waitForAsync
} from '@angular/core/testing';

import {ExpertHelpModalComponent} from './expert-help-modal.component';
import {MODAL_DEF} from 'src/app/modal/modal.providers';
import {of} from 'rxjs';

describe('ExpertHelpModalComponent', () => {
  let component: ExpertHelpModalComponent;
  let fixture: ComponentFixture<ExpertHelpModalComponent>;

  let expertHelpService: ExpertHelpService;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [MapModule, RouterTestingModule],
        providers: [
          {
            provide: MODAL_DEF,
            useValue: {
              id: EXPERT_HELP_MODAL_ID,
              size: 'lg'
            }
          }
        ]
      }).compileComponents();

      expertHelpService = TestBed.inject(ExpertHelpService);
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ExpertHelpModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should valid form', () => {
    const spyExpertHelpService = spyOn(
      expertHelpService,
      'sendHelpRequest$'
    ).and.returnValue(of());

    component.form.setValue({helpType: 'REMOTE', issueDescription: null});
    component.sendIssue$();

    expect(spyExpertHelpService).not.toHaveBeenCalled();

    component.form.setValue({helpType: null, issueDescription: 'test'});
    component.sendIssue$();

    expect(spyExpertHelpService).not.toHaveBeenCalled();

    component.form.setValue({helpType: 'test', issueDescription: null});
    component.sendIssue$();

    expect(spyExpertHelpService).not.toHaveBeenCalled();
  });
});
