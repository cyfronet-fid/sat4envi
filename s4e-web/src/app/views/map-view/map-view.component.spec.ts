import { environment } from 'src/environments/environment';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {MapViewComponent} from './map-view.component';
import {MapModule} from './map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {By} from '@angular/platform-browser';
import {MapService} from './state/map/map.service';
import {ActivatedRoute, ActivatedRouteSnapshot, UrlSegment} from '@angular/router';
import {take} from 'rxjs/operators';
import {MapStore} from './state/map/map.store';
import {SceneStore} from './state/scene/scene.store.service';
import {SceneFactory} from './state/scene/scene.factory.spec';
import {SessionStore} from '../../state/session/session.store';
import {LocalStorageTestingProvider, RemoteConfigurationTestingProvider} from '../../app.configuration.spec';
import waitForExpect from 'wait-for-expect';

describe('MapViewComponent', () => {
  let component: MapViewComponent;
  let sessionStore: SessionStore;
  let mapStore: MapStore;
  let fixture: ComponentFixture<MapViewComponent>;
  let route: ActivatedRoute;
  let sceneStore: SceneStore;

  function setActivatedChildPath(path: string) {
    route.snapshot = {
      children: [
        {
          url: [
            new UrlSegment(path, {})
          ]
        }
      ]
    } as ActivatedRouteSnapshot;
  }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider, RemoteConfigurationTestingProvider],
      imports: [MapModule, RouterTestingModule, HttpClientTestingModule]
    })
      .compileComponents();
    sessionStore = TestBed.get(SessionStore);
    mapStore = TestBed.get(MapStore);
    sceneStore = TestBed.get(SceneStore);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MapViewComponent);
    component = fixture.componentInstance;
    route = TestBed.get(ActivatedRoute);
    setActivatedChildPath('products');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should isLinkActive return true if sub path in router is equal to argument', () => {
    expect(component.isLinkActive('sentinel-search')).toBeFalsy();
    setActivatedChildPath('sentinel-search');
    expect(component.isLinkActive('sentinel-search')).toBeTruthy();
  });

  it('activated route should relate to isLinkActive', () => {
    setActivatedChildPath('sentinel-search');
    fixture.debugElement.queryAll(By.css('.section.sidebar > ul.switch:nth-child(1):not(.active)'));
    fixture.debugElement.queryAll(By.css('.section.sidebar > ul.switch:nth-child(2).active'));
    setActivatedChildPath('products');
    fixture.debugElement.queryAll(By.css('.section.sidebar > ul.switch:nth-child(1).active'));
    fixture.debugElement.queryAll(By.css('.section.sidebar > ul.switch:nth-child(2):not(.active)'));
  });

  describe('ZK Options', () => {
    it('should not show #zk-options-button if user is not memberZK', () => {
      sessionStore.update({memberZK: false});
      fixture.detectChanges();
      expect(fixture.debugElement.query(By.css('#zk-options-button'))).toBeNull();
    });

    it('should show #zk-options-button if user is not memberZK', () => {
      sessionStore.update({memberZK: true});
      fixture.detectChanges();
      expect(fixture.debugElement.query(By.css('#zk-options-button'))).not.toBeNull();
    });

    it('clicking #zk-options-button should show options', () => {
      // log in user
      sessionStore.update({memberZK: true});
      fixture.detectChanges();
      fixture.debugElement.query(By.css('#zk-options-button')).nativeElement.click();
      fixture.detectChanges();
      expect(fixture.debugElement.query(By.css('.dropdown__button--zk'))).toBeTruthy();
    });

    it('clicking .zk__dropdown__close should toggleZKOptions(false)', () => {
      // log in user
      sessionStore.update({memberZK: true});
      fixture.detectChanges();
      fixture.debugElement.query(By.css('#zk-options-button')).nativeElement.click();
      fixture.detectChanges();
      const spy = spyOn(component, 'toggleZKOptions');
      fixture.debugElement.query(By.css('.dropdown__close')).nativeElement.click();
      expect(spy).toHaveBeenCalledWith(false);
    });

    it('toggleZKOptions should call mapService.toggleZKOptions ', () => {
      const service: MapService = TestBed.get(MapService);
      const spy = spyOn(service, 'toggleZKOptions');
      component.toggleZKOptions();
      expect(spy).toHaveBeenCalledWith(true);
    });

    it('Download Original File button should be disabled if there is no activeScene ', async () => {
      sessionStore.update({memberZK: true});
      mapStore.update({zkOptionsOpened: true});
      const activeScene = await component.activeScene$.pipe(take(1)).toPromise();
      fixture.detectChanges();
      expect(activeScene).toBeFalsy();
      const link = fixture.debugElement.query(By.css('[data-test-download-original=\'\'].disabled'));
      expect(link).toBeTruthy();
    });
  });

  it('sidebarOpen$ should trigger map update size', async () => {
    const spy = spyOn(component.mapComponent, 'updateSize');
    mapStore.update({sidebarOpen: true});
    await waitForExpect(() => { expect(spy).toHaveBeenCalled(); });
  });
});
