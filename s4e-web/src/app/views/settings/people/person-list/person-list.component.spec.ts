import { InstitutionService } from './../../state/institution/institution.service';
import { PersonService } from './../state/person.service';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs';
import { PersonQuery } from './../state/person.query';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {PersonListComponent} from './person-list.component';
import {SettingsModule} from '../../settings.module';
import { DebugElement } from '@angular/core';

describe('PeopleComponent', () => {
  let component: PersonListComponent;
  let fixture: ComponentFixture<PersonListComponent>;
  let personQuery: PersonQuery;
  let institutionService: InstitutionService;
  let personService: PersonService;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SettingsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    })
      .compileComponents();

    personQuery = TestBed.get(PersonQuery);
    personService = TestBed.get(PersonService);
    institutionService = TestBed.get(InstitutionService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonListComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
