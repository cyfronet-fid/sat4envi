import { OverlayService } from './../../state/overlay/overlay.service';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import {OverlayListModalComponent} from './overlay-list-modal.component';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {RemoteConfigurationTestingProvider} from '../../../../app.configuration.spec';
import { of } from 'rxjs';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

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

  it('should valid values on add', async () => {
    const spy = spyOn(overlayService, 'createOverlay');

    const newLayer = {
      label: 'test',
      url: 'incorrect-url'
    };
    component.newOverlayForm.setValue(newLayer);
    component.showOverlayForm$ = of(true);
    component.addingNewLayer$ = of(false);
    fixture.detectChanges();

    await component.addNewOverlay();
    expect(spy).not.toHaveBeenCalled();
  });
  it('should valid url params on add', async () => {
    const spy = spyOn(overlayService, 'createOverlay');

    const url = [
      'https://test-page.pl/?SERVICE=unknown',
      'REQUEST=unknown',
      'LAYERS=unkn?own',
      'STYLES=unkn?own',
      'FORMAT=image/unknown',
      'TRANSPARENT=none',
      'VERSION=x.y.z',
      'HEIGHT=none',
      'WIDTH=none',
      'CRS=unknown',
      'BBOX=unknown,unknown,unknown,unknown'
    ].join('&');
    const newLayer = {
      label: 'test',
      url
    };
    component.newOverlayForm.setValue(newLayer);
    component.showOverlayForm$ = of(true);
    component.addingNewLayer$ = of(false);
    fixture.detectChanges();

    await component.addNewOverlay();
    expect(spy).not.toHaveBeenCalled();
  });
});
