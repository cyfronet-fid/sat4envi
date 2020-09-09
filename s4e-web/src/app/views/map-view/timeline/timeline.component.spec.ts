import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {TimelineComponent} from './timeline.component';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MapModule} from '../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {RemoteConfigurationTestingProvider} from '../../../app.configuration.spec';
import moment from 'moment';
import {SimpleChange} from '@angular/core';

describe('TimelineComponent', () => {
  let component: TimelineComponent;
  let fixture: ComponentFixture<TimelineComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, NoopAnimationsModule, HttpClientTestingModule, RouterTestingModule],
      providers: [RemoteConfigurationTestingProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate hourmarks', () => {
    component.resolution = 12;
    component.startTime = moment('2020-09-09');
    component.ngOnChanges({
      startTime: new SimpleChange(null, component.startTime, false),
      resolution: new SimpleChange(24, component.resolution, false)
    });
    expect(component.hourmarks).toEqual(
      [
        '00:00',
        '02:00',
        '04:00',
        '06:00',
        '08:00',
        '10:00']);
  });
});
