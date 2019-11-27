import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MapComponent} from './map.component';
import {TestingConfigProvider} from '../../../app.configuration.spec';
import {ShareModule} from '../../../common/share.module';

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [
        TestingConfigProvider,
      ],
      imports: [
        ShareModule
      ],
      declarations: [MapComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    component.ngOnInit();
  });

  it('should download snapshot', () => {
    const spy = spyOn((component as any).map, 'renderSync');
    const spy2 = spyOn(component.linkDownload.nativeElement, 'click');

    spyOn((component as any).map, 'once').and.callFake((event, callback) => {
      expect(event).toBe('rendercomplete');
      callback();
      expect(component.linkDownload.nativeElement.getAttribute('download')).toContain('SNAPSHOT');
      expect(component.linkDownload.nativeElement.getAttribute('href')).toEqual('data:image/png;base64,00')
    });


    component.downloadMap();
    expect(spy).toHaveBeenCalled();
    expect(spy2).toHaveBeenCalled();
  });
});
