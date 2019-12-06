import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SentinelSearchComponent } from './sentinel-search.component';

describe('SentinelSearchComponent', () => {
  let component: SentinelSearchComponent;
  let fixture: ComponentFixture<SentinelSearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SentinelSearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SentinelSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
