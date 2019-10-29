import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckBoxListComponent } from './check-box-list.component';

describe('CheckBoxListComponent', () => {
  let component: CheckBoxListComponent;
  let fixture: ComponentFixture<CheckBoxListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CheckBoxListComponent ]
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
