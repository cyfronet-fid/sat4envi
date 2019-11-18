import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupFormComponent } from './group-form.component';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterTestingModule} from '@angular/router/testing';
import {S4EFormsModule} from '../../../../utils/s4e-forms/s4e-forms.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';

describe('GroupFormComponent', () => {
  let component: GroupFormComponent;
  let fixture: ComponentFixture<GroupFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GroupFormComponent ],
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
    fixture = TestBed.createComponent(GroupFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
