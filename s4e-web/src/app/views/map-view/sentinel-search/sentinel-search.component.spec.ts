import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SentinelSearchComponent} from './sentinel-search.component';
import {MapModule} from '../map.module';
import {ShareModule} from '../../../common/share.module';
import {RouterTestingModule} from '@angular/router/testing';
import {OwlDateTimeModule, OwlNativeDateTimeModule} from "ng-pick-datetime";
import {NoopAnimationsModule} from "@angular/platform-browser/animations";
import {TestingConfigProvider} from '../../../app.configuration.spec';

describe('SentinelSearchComponent', () => {
  let component: SentinelSearchComponent;
  let fixture: ComponentFixture<SentinelSearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [OwlDateTimeModule, OwlNativeDateTimeModule, NoopAnimationsModule,
        ShareModule, MapModule, RouterTestingModule],
      providers: [TestingConfigProvider]
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
