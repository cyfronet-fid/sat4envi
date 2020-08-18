import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {OverlayListModalComponent} from './overlay-list-modal.component';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {RemoteConfigurationTestingProvider} from '../../../../app.configuration.spec';

describe('OverlayListModalComponent', () => {
  let component: OverlayListModalComponent;
  let fixture: ComponentFixture<OverlayListModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
      providers: [RemoteConfigurationTestingProvider]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OverlayListModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
