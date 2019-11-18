import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckBoxListComponent } from './check-box-list.component';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterTestingModule} from '@angular/router/testing';
import {S4EFormsModule} from '../s4e-forms.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {TestingConfigProvider} from '../../../app.configuration.spec';

describe('CheckBoxListComponent', () => {
  let component: CheckBoxListComponent<any>;
  let fixture: ComponentFixture<CheckBoxListComponent<any>>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CheckBoxListComponent ],
      imports: [
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        TestingConfigProvider
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckBoxListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
