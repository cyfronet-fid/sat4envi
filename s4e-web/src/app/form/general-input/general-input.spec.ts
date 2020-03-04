import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {Component} from '@angular/core';
import {ControlContainer} from '@angular/forms';
import {GeneralInput} from './general-input';

@Component({
  selector: 'mock-modal',
  template: '',
  styles: []
})
class MockGeneralInput extends GeneralInput {
  constructor(cc: ControlContainer) {
    super(cc, undefined);
  }
}

class MockExtFormDirective {
  disabled: boolean = false;
  labelSize: number = 4;
  isLoading: boolean = false;
}

class MockControlContainer {
}

describe('GeneralInput', () => {
  let component: GeneralInput;
  let fixture: ComponentFixture<MockGeneralInput>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MockGeneralInput],
      imports: [],
      providers: [{provide: ControlContainer, useClass: MockControlContainer}]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MockGeneralInput);
    component = fixture.componentInstance;
    component.controlId = 'general';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
