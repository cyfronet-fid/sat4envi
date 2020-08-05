import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {DatepickerComponent} from './datepicker.component';
import {S4EFormsModule} from '../form.module';

describe('DatepickerComponent', () => {
  let component: DatepickerComponent;
  let fixture: ComponentFixture<DatepickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [S4EFormsModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatepickerComponent);
    component = fixture.componentInstance;
    component.controlId = 'date-picker';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
