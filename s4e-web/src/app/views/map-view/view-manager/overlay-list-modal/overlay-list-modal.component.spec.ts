import { OverlayService } from './../../state/overlay/overlay.service';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import {OverlayListModalComponent} from './overlay-list-modal.component';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {RemoteConfigurationTestingProvider} from '../../../../app.configuration.spec';
import { DebugElement } from '@angular/core';

describe('OverlayListModalComponent', () => {
  let component: OverlayListModalComponent;
  let fixture: ComponentFixture<OverlayListModalComponent>;
  let de: DebugElement;
  let overlayService: OverlayService;

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
    de = fixture.debugElement;
    overlayService = TestBed.get(OverlayService);
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
});
