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

import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ParentInstitutionModalComponent} from './parent-institution-modal.component';
import {ManageInstitutionsModule} from '../manage-institutions.module';
import {MODAL_DEF} from 'src/app/modal/modal.providers';
import {createModal} from 'src/app/modal/state/modal.model';
import {
  PARENT_INSTITUTION_MODAL_ID,
  ParentInstitutionModal
} from './parent-institution-modal.model';
import {of} from 'rxjs';
import {DebugElement} from '@angular/core';
import {InstitutionQuery} from '../../state/institution/institution.query';

describe('ParentInstitutionModalComponent', () => {
  let component: ParentInstitutionModalComponent;
  let fixture: ComponentFixture<ParentInstitutionModalComponent>;
  let de: DebugElement;
  let institutionQuery: InstitutionQuery;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [
          ManageInstitutionsModule,
          HttpClientTestingModule,
          RouterTestingModule
        ],
        providers: [
          {
            provide: MODAL_DEF,
            useValue: createModal({
              id: PARENT_INSTITUTION_MODAL_ID,
              size: 'lg',
              hasReturnValue: true,
              returnValue: null
            } as ParentInstitutionModal)
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ParentInstitutionModalComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;

    institutionQuery = TestBed.inject(InstitutionQuery);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show all institutions on init', () => {
    const institutions = [{name: 'Straz pozarna'}, {name: 'Pogotowie'}];
    spyOn(institutionQuery, 'selectAdministrationInstitutions$').and.returnValue(
      of(institutions)
    );

    fixture.detectChanges();

    const institutionsLabels = de.nativeElement.querySelectorAll(
      'ul > li > label[data-e2e="institution-radio"]'
    );
    expect(institutionsLabels.length).toEqual(institutions.length);
    for (let i = 0; i < institutions.length; i++) {
      expect(institutionsLabels[i].innerHTML).toContain(institutions[i].name);
    }

    expect(component.searchedInstitutions).toEqual(institutions);
  });

  it('should show search institution on input value change', () => {
    const searchValue = 'Straz';
    const searchedInstitution = {name: 'Straz pozarna'};
    const institutions = [searchedInstitution, {name: 'Pogotowie'}];
    spyOn(institutionQuery, 'selectAdministrationInstitutions$').and.returnValue(
      of(institutions)
    );

    fixture.detectChanges();

    component.institutionsSearch.setValue(searchValue);
    fixture.detectChanges();

    const institutionsLabels = de.nativeElement.querySelectorAll(
      'ul > li > label[data-e2e="institution-radio"]'
    );
    expect(institutionsLabels.length).toEqual(1);
    expect(institutionsLabels[0].innerHTML).toContain(institutions[0].name);

    expect(component.searchedInstitutions).toEqual([searchedInstitution]);
  });

  it('should enabled apply btn on institution selection', () => {
    const institutions = [{name: 'Straz pozarna'}, {name: 'Pogotowie'}];
    spyOn(institutionQuery, 'selectAdministrationInstitutions$').and.returnValue(
      of(institutions)
    );

    fixture.detectChanges();

    const applyBtn = de.nativeElement.querySelector('button[type=submit]');
    expect(applyBtn.getAttribute('disabled')).toEqual(null);
    const institutionLabel = de.nativeElement.querySelector(
      'ul > li > label[data-e2e="institution-radio"]'
    );
    institutionLabel.click();

    fixture.detectChanges();

    expect(component.selectedInstitution).toEqual(institutions[0]);
    expect(applyBtn.getAttribute('disabled')).toEqual(null);
  });
});
