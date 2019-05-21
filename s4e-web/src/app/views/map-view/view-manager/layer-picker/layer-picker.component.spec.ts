import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LayerPicker } from './layer-picker.component';

describe('LayerPicker', () => {
  let component: LayerPicker;
  let fixture: ComponentFixture<LayerPicker>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LayerPicker ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LayerPicker);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
