import { By } from '@angular/platform-browser';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { S4eConfig } from './../../../../utils/initializer/config.service';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ParentInstitutionModalComponent } from './parent-institution-modal.component';
import { ManageInstitutionsModalModule } from '../manage-institutions.module';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { createModal } from 'src/app/modal/state/modal.model';
import { PARENT_INSTITUTION_MODAL_ID, ParentInstitutionModal } from './parent-institution-modal.model';
import { of } from 'rxjs';
import { DebugElement } from '@angular/core';
import { InstitutionQuery } from '../../state/institution/institution.query';

describe('ParentInstitutionModalComponent', () => {
  let component: ParentInstitutionModalComponent;
  let fixture: ComponentFixture<ParentInstitutionModalComponent>;
  let de: DebugElement;
  let institutionQuery: InstitutionQuery;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ManageInstitutionsModalModule,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        S4eConfig,
        {
          provide: MODAL_DEF, useValue: createModal({
            id: PARENT_INSTITUTION_MODAL_ID,
            size: 'lg',
            hasReturnValue: true,
            returnValue: null
          } as ParentInstitutionModal)
        }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ParentInstitutionModalComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;

    institutionQuery = TestBed.get(InstitutionQuery);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show all institutions on init', () => {
    const institutions = [
      { name: 'Straz pozarna' },
      { name: 'Pogotowie' }
    ];
    spyOn(institutionQuery, 'selectAll').and.returnValue(of(institutions));

    fixture.detectChanges();

    const institutionsLabels = de.nativeElement.querySelectorAll('ul > li > label[for=institution]');
    expect(institutionsLabels.length).toEqual(institutions.length);
    for (let i = 0; i < institutions.length; i++) {
      expect(institutionsLabels[i].innerHTML).toContain(institutions[i].name);
    }

    expect(component.searchedInstitutions).toEqual(institutions);
  });

  it('should show search institution on input value change', () => {
    const searchValue = 'Straz';
    const searchedInstitution = { name: 'Straz pozarna' };
    const institutions = [
      searchedInstitution,
      { name: 'Pogotowie' }
    ];
    spyOn(institutionQuery, 'selectAll').and.returnValue(of(institutions));

    fixture.detectChanges();

    component.institutionsSearch.setValue(searchValue);
    fixture.detectChanges();

    const institutionsLabels = de.nativeElement.querySelectorAll('ul > li > label[for=institution]');
    expect(institutionsLabels.length).toEqual(1);
    expect(institutionsLabels[0].innerHTML).toContain(institutions[0].name);

    expect(component.searchedInstitutions).toEqual([searchedInstitution]);
  });

  it('should enabled apply btn on institution selection', () => {
    const institutions = [
      { name: 'Straz pozarna' },
      { name: 'Pogotowie' }
    ];
    spyOn(institutionQuery, 'selectAll').and.returnValue(of(institutions));

    fixture.detectChanges();

    const applyBtn = de.nativeElement.querySelector('button[type=submit]');
    expect(applyBtn.getAttribute('disabled')).toEqual(null);
    const institutionLabel = de.nativeElement.querySelector('ul > li > label[for=institution]');
    institutionLabel.click();

    fixture.detectChanges();

    expect(component.selectedInstitution).toEqual(institutions[0]);
    expect(applyBtn.getAttribute('disabled')).toEqual(null);
  });
});
