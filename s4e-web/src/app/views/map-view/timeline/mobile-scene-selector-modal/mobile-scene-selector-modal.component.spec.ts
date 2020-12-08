import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MobileSceneSelectorModalComponent } from './mobile-scene-selector-modal.component';

describe('MobileSceneSelectorModalComponent', () => {
  let component: MobileSceneSelectorModalComponent;
  let fixture: ComponentFixture<MobileSceneSelectorModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MobileSceneSelectorModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MobileSceneSelectorModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
