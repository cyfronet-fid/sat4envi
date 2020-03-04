import {ExtFormDirective} from './ext-form.directive';
import {Component, DebugElement, Input} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {S4EFormsModule} from '../form.module';
import {TestingConfigProvider} from '../../app.configuration.spec';


/**
 * This directive is used mainly to manipulate DOM,
 * that's why the easiest way to test it is to create
 * custom mock component which uses it, and test how
 * the directive changes component's content
 */
@Component({
  selector: 'mock-directive',
  template: '<form extForm [isLoading]="isLoading"></form>',
  styles: [],
})
class MockDirectiveComponent {
  @Input() isLoading: boolean = false;
}


describe('ExtFormDirective', () => {
  let component: MockDirectiveComponent;
  let fixture: ComponentFixture<MockDirectiveComponent>;
  let element: DebugElement;
  let directive: ExtFormDirective;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [S4EFormsModule],
      declarations: [MockDirectiveComponent],
      providers: [TestingConfigProvider]
    });

    TestBed.compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MockDirectiveComponent);
    component = fixture.componentInstance;
    element = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    expect(component).toBeTruthy();
  });

  it('should properly show and hide loading', async () => {
    component.isLoading = true;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(element.queryAll(By.css('ext-spinner')).length).toEqual(1);

    component.isLoading = false;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(element.queryAll(By.css('ext-spinner')).length).toEqual(0);
  });

  it('should add ext-form css class', () => {
    expect(element.queryAll(By.css('.ext-form')).length).toEqual(1);
  });
});
