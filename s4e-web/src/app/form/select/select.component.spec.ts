import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SelectComponent} from './select.component';
import {Component} from '@angular/core';
import {By} from '@angular/platform-browser';
import {S4EFormsModule} from '../form.module';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'test-select-container',
  styles: [],
  template: '<form [formGroup]="form" novalidate extForm>' +
  '<ext-select formControlName="select">' +
  '<option *ngFor="let option of options" [ngValue]="option">{{option.id}}</option>' +
  '</ext-select>' +
  '</form>'
})
class TestSelectContainerComponent {
  form: FormGroup = new FormGroup({select: new FormControl(null)});
  options: any[] = [{id: 0}, {id: 1}];
}

/**
 * This test component is used to test whether option directive does not break
 * standard <select> tag
 */
@Component({
  selector: 'test-normal-select-container',
  styles: [],
  template: '<form [formGroup]="form">' +
  '<select formControlName="select">' +
  '<option *ngFor="let option of options" [ngValue]="option">{{option.id}}</option>' +
  '</select>' +
  '</form>'
})
class TestNormalSelectContainerComponent {
  form: FormGroup = new FormGroup({select: new FormControl(null)});
  options: any[] = [{id: 0}, {id: 1}];
}

describe('SelectComponent', () => {
  describe('UnitTests', () => {

    let component: SelectComponent;
    let fixture: ComponentFixture<SelectComponent>;

    beforeEach(async(() => {
      TestBed.configureTestingModule({
        imports: [S4EFormsModule, ReactiveFormsModule],
        declarations: [TestNormalSelectContainerComponent]
      })
        .compileComponents();
    }));

    beforeEach(() => {
      fixture = TestBed.createComponent(SelectComponent);
      component = fixture.componentInstance;
      component.controlId = 'select';
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('Standard <select> tag', () => {
    let component: TestNormalSelectContainerComponent;
    let fixture: ComponentFixture<TestNormalSelectContainerComponent>;

    beforeEach(async(() => {
      TestBed.configureTestingModule({
        imports: [S4EFormsModule, ReactiveFormsModule],
        declarations: [TestNormalSelectContainerComponent]
      })
        .compileComponents();
    }));

    beforeEach(async () => {
      fixture = TestBed.createComponent(TestNormalSelectContainerComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should not be broken by custom <option> directive', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('Integration Tests', () => {
    let component: TestSelectContainerComponent;
    let fixture: ComponentFixture<TestSelectContainerComponent>;

    beforeEach(async(() => {
      TestBed.configureTestingModule({
        imports: [S4EFormsModule, ReactiveFormsModule],
        declarations: [TestSelectContainerComponent]
      })
        .compileComponents();
    }));

    beforeEach(async () => {
      fixture = TestBed.createComponent(TestSelectContainerComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      await fixture.whenStable();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should properly change value in ngValue', async () => {
      const select = fixture.debugElement.query(By.css('select')).nativeElement;
      select.value = '0: Object';
      select.dispatchEvent(new Event('change'));
      fixture.detectChanges();

      expect(component.form.value.select).toEqual({id: 0});

      select.value = '1: Object';
      select.dispatchEvent(new Event('change'));
      fixture.detectChanges();
      expect(component.form.value.select).toEqual({id: 1});
    });

    it('should reflect changes after setting value to the form', async () => {
      component.form.setValue({select: component.options[1]});
      fixture.detectChanges();
      await fixture.whenStable();

      expect(fixture.debugElement.query(By.css('select')).nativeElement.value).toEqual('1: Object');
    });

    it('should have select.disabled set when disabled', async () => {
      component.form.disable();
      fixture.detectChanges();
      await fixture.whenStable();

      expect((fixture.debugElement.query(By.css('select')).nativeElement as HTMLElement).getAttribute('disabled')).not.toBeUndefined();
    });
  });
});
