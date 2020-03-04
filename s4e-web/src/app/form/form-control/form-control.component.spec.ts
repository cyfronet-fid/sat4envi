import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControlComponent} from './form-control.component';
import {S4EFormsModule} from '../form.module';
import {TestingConfigProvider} from '../../app.configuration.spec';

describe('FormControlComponent', () => {
  let component: FormControlComponent;
  let fixture: ComponentFixture<FormControlComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [S4EFormsModule],
      providers: [TestingConfigProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FormControlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
