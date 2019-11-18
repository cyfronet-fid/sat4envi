import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupListComponent } from './group-list.component';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterTestingModule} from '@angular/router/testing';
import {S4EFormsModule} from '../../../../utils/s4e-forms/s4e-forms.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';

describe('GroupListComponent', () => {
  let component: GroupListComponent;
  let fixture: ComponentFixture<GroupListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GroupListComponent ],
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
    fixture = TestBed.createComponent(GroupListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
