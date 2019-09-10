import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MapComponent } from './map.component';
import {RecentViewQuery} from '../state/recent-view/recent-view.query';
import {TestingConfigProvider} from '../../../app.configuration.spec';
import {TestingConstantsProvider} from '../../../app.constants.spec';

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [
        TestingConstantsProvider,
        TestingConfigProvider,
        RecentViewQuery
      ],
      declarations: [ MapComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    component.ngOnInit();
  });
});
