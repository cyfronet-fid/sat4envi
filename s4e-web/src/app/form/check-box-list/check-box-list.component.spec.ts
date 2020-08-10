import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CheckBoxListComponent} from './check-box-list.component';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('CheckBoxListComponent', () => {
  let component: CheckBoxListComponent<any>;
  let fixture: ComponentFixture<CheckBoxListComponent<any>>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CheckBoxListComponent],
      imports: [
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule
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
