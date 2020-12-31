import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MobileSceneSelectorModalComponent } from './mobile-scene-selector-modal.component';
import {MapModule} from '../../map.module';
import {LocalStorageTestingProvider, RemoteConfigurationTestingProvider} from '../../../../app.configuration.spec';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('MobileSceneSelectorModalComponent', () => {
  let component: MobileSceneSelectorModalComponent;
  let fixture: ComponentFixture<MobileSceneSelectorModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, RouterTestingModule, HttpClientTestingModule],
      providers: [RemoteConfigurationTestingProvider, LocalStorageTestingProvider]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MobileSceneSelectorModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
