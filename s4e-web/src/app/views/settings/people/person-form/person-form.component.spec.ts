import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonFormComponent } from './person-form.component';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {S4EFormsModule} from '../../../../utils/s4e-forms/s4e-forms.module';
import {RouterTestingModule} from '@angular/router/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';

describe('PersonFormComponent', () => {
  let component: PersonFormComponent;
  let fixture: ComponentFixture<PersonFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PersonFormComponent ],
      imports: [
        ReactiveFormsModule,
        RouterTestingModule,
        S4EFormsModule,
        HttpClientTestingModule
      ],
      providers: [
        TestingConfigProvider
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
