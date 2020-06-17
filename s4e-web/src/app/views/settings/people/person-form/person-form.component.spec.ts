import { PersonFactory } from './../state/person.factory.spec';
import { PERSON_FORM_MODAL_ID, PersonFormModal } from './person-form-modal.model';
import { InstitutionService } from './../../state/institution/institution.service';
import { PersonService } from './../state/person.service';
import { By } from '@angular/platform-browser';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import {PersonFormComponent} from './person-form.component';
import {RouterTestingModule} from '@angular/router/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {SettingsModule} from '../../settings.module';
import { of } from 'rxjs';
import { DebugElement } from '@angular/core';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { createModal } from 'src/app/modal/state/modal.model';

describe('PersonFormComponent', () => {
  let component: PersonFormComponent;
  let fixture: ComponentFixture<PersonFormComponent>;
  let personService: PersonService;
  let institutionService: InstitutionService;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SettingsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        TestingConfigProvider,
        {provide: MODAL_DEF, useValue: createModal({id: PERSON_FORM_MODAL_ID, size: 'lg'} as PersonFormModal)}
      ]
    })
      .compileComponents();

    personService = TestBed.get(PersonService);
    institutionService = TestBed.get(InstitutionService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonFormComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
  it('should add person', fakeAsync(() => {
    const person = PersonFactory.build();
    const institutionSlug = 'test#1';
    spyOn(institutionService, 'connectInstitutionToQuery$').and.returnValue(of(institutionSlug));
    const spySave = spyOn(personService, 'create$').and.returnValue(of(1));
    const spyDismiss = spyOn(component, 'dismiss');
    tick();
    fixture.detectChanges();

    component.form.patchValue(person);
    fixture.detectChanges();

    const saveBtn = de.query(By.css('button[data-test="save"]'));
    saveBtn.nativeElement.click();

    tick();

    expect(spyDismiss).toHaveBeenCalled();
    expect(spySave).toHaveBeenCalledWith(institutionSlug, component.form.value);
  }));
});
