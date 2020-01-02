import {SaveConfigModalComponent} from './save-config-modal.component';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {SAVE_CONFIG_MODAL_ID} from './save-config-modal.model';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {ViewConfigurationService} from '../../state/view-configuration/view-configuration.service';
import {ViewConfigurationQuery} from '../../state/view-configuration/view-configuration.query';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {MapModule} from '../../map.module';
import {By} from '@angular/platform-browser';
import {ViewConfiguration, ViewConfigurationEx} from '../../state/view-configuration/view-configuration.model';
import {of} from 'rxjs';

describe('SaveConfigModalComponent', () => {
  let component: SaveConfigModalComponent;
  let fixture: ComponentFixture<SaveConfigModalComponent>;
  let service: ViewConfigurationService;
  const viewConf: ViewConfigurationEx = {
    caption: '',
    configuration: {
      overlays: ['ov1', 'ov3'],
      productId: 51,
      sceneId: 101,
      date: '11-11-2020',
      viewPosition: {
        zoomLevel: 10,
        centerCoordinates: [56, 67]
      }
    },
    configurationNames: {overlays: ['overlay 1', 'overlay 3'], product: 'product 1', selectedDate: '11-11-2020'},
    thumbnail: 'data:image/png;base64,00'
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule],
      providers: [
        TestingConfigProvider,
        ViewConfigurationService,
        ViewConfigurationQuery,
        ModalQuery,
        {
          provide: MODAL_DEF, useValue: {
            id: SAVE_CONFIG_MODAL_ID,
            size: 'lg',
            viewConfiguration: viewConf
          }
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SaveConfigModalComponent);
    component = fixture.componentInstance;
    service = TestBed.get(ViewConfigurationService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('submitting form should call accept', () => {
    const spy = spyOn(component, 'accept').and.stub();
    fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement.click();
    expect(spy).toHaveBeenCalled();
  });

  it('configuration name should be initially taken from selected date', () => {
    const spy = jest.spyOn(service, 'add$');
    spy.mockReturnValueOnce(of(true));

    const expected: ViewConfiguration = {
      ...viewConf,
      thumbnail: '00',
      caption: '11-11-2020'
    };

    fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement.click();
    expect(spy).toHaveBeenCalledWith(expected);
  });

  it('accepting form should call service', () => {
    const spy = jest.spyOn(service, 'add$');
    spy.mockReturnValueOnce(of(true));

    const configuratioNameInput = component.form.controls.configurationName;
    configuratioNameInput.setValue('new name');

    const expected: ViewConfiguration = {
      ...viewConf,
      thumbnail: '00',
      caption: 'new name'
    };

    fixture.debugElement.query(By.css('button[type="submit"]')).nativeElement.click();
    expect(spy).toHaveBeenCalledWith(expected);
  });

  it('accepting form should close modal', async () => {
    spyOn(service, 'add$').and.returnValue(of(true));
    const dimissSpy = jest.spyOn(component, 'dismiss');

    await component.accept();
    expect(dimissSpy).toHaveBeenCalled();
  });
});
