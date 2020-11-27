import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {of, throwError} from 'rxjs';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import {RemoteConfigurationTestingProvider} from '../../app.configuration.spec';
import {MapModule} from '../../views/map-view/map.module';
import {OverlayService} from '../../views/map-view/state/overlay/overlay.service';
import {OverlayListComponent} from './overlay-list.component';
import {catchError} from 'rxjs/operators';

describe('OverlayListModalComponent', () => {
  let component: OverlayListComponent;
  let fixture: ComponentFixture<OverlayListComponent>;
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
    fixture = TestBed.createComponent(OverlayListComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    overlayService = TestBed.get(OverlayService);
  });

  it('should create', () => {
    component.newOwner = 'PERSONAL';
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should valid values on add', fakeAsync(() => {
    spyOn(component, 'hasLoadingError$')
      .and.returnValue(of().pipe(catchError(() => throwError(''))));
    const spy = spyOn(overlayService, 'createPersonalOverlay');

    const newLayer = {
      label: 'test',
      url: 'incorrect-url'
    };
    component.newOwner = 'PERSONAL';
    component.newOverlayForm.setValue(newLayer);
    component.addNewOverlay();

    tick();

    expect(spy).not.toHaveBeenCalled();
  }));
  it('should valid url params on add', async () => {
    component.newOwner = 'PERSONAL';
    const spy = spyOn(overlayService, 'createPersonalOverlay');

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

    await component.addNewOverlay();
    expect(spy).not.toHaveBeenCalled();
  });
});
