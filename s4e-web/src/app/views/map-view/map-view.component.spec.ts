import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import {MapViewComponent} from './map-view.component';
import {MapModule} from './map.module';
import {TestingConfigProvider} from '../../app.configuration.spec';
import {RouterTestingModule} from '@angular/router/testing';
import {By} from '@angular/platform-browser';
import {MapService} from './state/map/map.service';
import {ProfileStore} from '../../state/profile/profile.store';


describe('MapViewComponent', () => {
  let component: MapViewComponent;
  let profileStore: ProfileStore;
  let fixture: ComponentFixture<MapViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, RouterTestingModule],
      providers: [TestingConfigProvider]
    })
      .compileComponents();
    profileStore = TestBed.get(ProfileStore);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MapViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ZK Options', () => {
    it('should not show #zk-options-button if user is not memberZK', () => {
      profileStore.update({memberZK: false});
      fixture.detectChanges();
      expect(fixture.debugElement.query(By.css('#zk-options-button'))).toBeNull();
    });

    it('should show #zk-options-button if user is not memberZK', () => {
      profileStore.update({memberZK: true});
      fixture.detectChanges();
      expect(fixture.debugElement.query(By.css('#zk-options-button'))).not.toBeNull();
    });

    it('clicking #zk-options-button should show options', () => {
      // log in user
      profileStore.update({memberZK: true});
      fixture.detectChanges();
      fixture.debugElement.query(By.css('#zk-options-button')).nativeElement.click();
      fixture.detectChanges();
      expect(fixture.debugElement.query(By.css('.zk_dropdown_button'))).toBeTruthy();
    });

    it('clicking .zk__dropdown__close should toggleZKOptions(false)', () => {
      // log in user
      profileStore.update({memberZK: true});
      fixture.detectChanges();
      fixture.debugElement.query(By.css('#zk-options-button')).nativeElement.click();
      fixture.detectChanges();
      const spy = spyOn(component, 'toggleZKOptions');
      fixture.debugElement.query(By.css('.zk__dropdown__close')).nativeElement.click();
      expect(spy).toHaveBeenCalledWith(false);
    });

    it('toggleZKOptions should call mapService.toggleZKOptions ', () => {
      const service: MapService = TestBed.get(MapService);
      const spy = spyOn(service, 'toggleZKOptions');
      component.toggleZKOptions();
      expect(spy).toHaveBeenCalledWith(true);
    });
  });
});
