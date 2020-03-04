import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {CheckboxComponent} from './checkbox.component';
import {RouterTestingModule} from '@angular/router/testing';
import {ControlContainer} from '@angular/forms';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import {S4EFormsModule} from '../form.module';
import {TestingConfigProvider} from '../../app.configuration.spec';

class MockedControlContainer {
  hasError(errorCode: string, path?: string[]): boolean {
    return false;
  }

  getError(errorCode: string, path?: string[]): any {
    return {};
  }
}

describe('CheckboxComponent', () => {
  let component: CheckboxComponent;
  let element: DebugElement;
  let fixture: ComponentFixture<CheckboxComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ S4EFormsModule, RouterTestingModule],
      providers: [{provide: ControlContainer, useClass: MockedControlContainer}, TestingConfigProvider]
    });
    TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CheckboxComponent);
    component = fixture.componentInstance;
    component.controlId = 'checkbox';
    element = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle', async () => {
    let currentValue: boolean;

    component.registerOnChange(val => currentValue = val);
    element.query(By.css('input[type="checkbox"]')).nativeElement.click();
    expect(currentValue).toBeTruthy();
    element.query(By.css('input[type="checkbox"]')).nativeElement.click();
    // noinspection JSUnusedAssignment - overwritten by component.registerOnChange
    expect(currentValue).toBeFalsy();
  });

  it('should toggle after clicking label', () => {
    let currentValue: boolean;

    component.registerOnChange(val => currentValue = val);
    element.query(By.css('label')).nativeElement.click();
    // noinspection JSUnusedAssignment - overwritten by component.registerOnChange
    expect(currentValue).toBeTruthy();
  });
});
