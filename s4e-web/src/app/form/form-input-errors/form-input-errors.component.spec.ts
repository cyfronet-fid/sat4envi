import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FormInputErrorsComponent } from './form-input-errors.component';

describe('FormInputErrorsComponent', () => {
  let component: FormInputErrorsComponent;
  let fixture: ComponentFixture<FormInputErrorsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FormInputErrorsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FormInputErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
