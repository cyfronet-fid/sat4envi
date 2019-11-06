import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LegendComponent } from './legend.component';
import {LegendFactory} from '../state/legend/legend.factory.spec';
import {By} from '@angular/platform-browser';

describe('LegendComponent', () => {
  let component: LegendComponent;
  let fixture: ComponentFixture<LegendComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LegendComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LegendComponent);
    component = fixture.componentInstance;
    component.isOpen = false;
    component.activeLegend = LegendFactory.build();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show description if isOpen is true', () => {
    component.isOpen = true;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('.desc-container'))).toBeTruthy()
  });
});
