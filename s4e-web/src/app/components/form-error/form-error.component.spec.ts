import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormErrorComponent } from './form-error.component';
import {FormErrorModule} from './form-error.module';
import {FormControl} from '@ng-stack/forms';
import {By} from '@angular/platform-browser';

describe('FormErrorComponent', () => {
  let component: FormErrorComponent;
  let fixture: ComponentFixture<FormErrorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormErrorModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FormErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not display if control was not touched', () => {
    const control = new FormControl<string>('');
    control.setErrors({'server': ['This password is too short']});
    component.control = control;
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('li')).length).toBe(0);
  });

  it('should display server messages correctly', () => {
    const control = new FormControl<string>('');
    const errorMessage = 'This password is too short';
    control.setErrors({'server': [errorMessage]});
    control.markAsTouched();
    component.control = control;
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('li'))[0].nativeElement.textContent).toContain(errorMessage);
  });
});
