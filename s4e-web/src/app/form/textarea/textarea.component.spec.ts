import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TextareaComponent} from './textarea.component';
import {ControlContainer, FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {Component, DebugElement} from '@angular/core';
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

@Component({
  template: '<form [formGroup]="form"><ext-textarea formControlName="content"></ext-textarea></form>'
})
class TestHostComponent {
  form: FormGroup;

  constructor(fb: FormBuilder) {
    this.form = fb.group({
      content: ''
    });
  }
}


describe('InputComponent', () => {
  describe('self component', () => {
    let component: TextareaComponent;
    let fixture: ComponentFixture<TextareaComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [S4EFormsModule, ReactiveFormsModule],
        declarations: [TestHostComponent],
        providers: [{provide: ControlContainer, useClass: MockedControlContainer}, TestingConfigProvider]
      })
        .compileComponents();
    });

    beforeEach(() => {
      fixture = TestBed.createComponent(TextareaComponent);
      component = fixture.componentInstance;
      component.controlId = 'input';
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('hosted in component', () => {
    let component: TestHostComponent;
    let element: DebugElement;
    let fixture: ComponentFixture<TestHostComponent>;

    beforeEach(() => {
      TestBed.configureTestingModule({
        declarations: [TestHostComponent],
        imports: [S4EFormsModule, ReactiveFormsModule],
        providers: [TestingConfigProvider]
      }).compileComponents();
    });

    beforeEach(() => {
      fixture = TestBed.createComponent(TestHostComponent);
      component = fixture.componentInstance;
      element = fixture.debugElement;
      fixture.detectChanges();
    });

    function typeText(text: string) {
      // #content is taken from TestHostComponent form definition
      const el: HTMLInputElement = element.query(By.css('#content')).nativeElement;
      el.value = text;
      el.dispatchEvent(new Event('input'));
      fixture.detectChanges();
    }

    it('should not have button by default', () => {
      expect(element.query(By.css('button'))).toBeFalsy();
    });
  });
});
