import {TestBed} from '@angular/core/testing';
import {TimelineService} from './timeline.service';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ProductService} from '../product/product.service';

describe('SceneService', () => {
  let timelineService: TimelineService;
  let productService: ProductService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TimelineService,
        LocalStorageTestingProvider
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule
      ]
    })
      .compileComponents();
    timelineService = TestBed.get(TimelineService);
    productService = TestBed.get(ProductService);
  });

  it('should create', () => {
    expect(timelineService).toBeTruthy();
  });

  it('should turn on live mode', () => {
    const spyLoadingLatestScene = spyOn(productService, 'getLastAvailableScene$')
      .and.returnValue(null);
    timelineService.toggleLiveMode();
    expect(spyLoadingLatestScene).toHaveBeenCalled();
    expect((timelineService as any)._updaterIntervalID).not.toBe(null);
  });
  it('should turn off live mode', () => {
    const spyLoadingLatestScene = spyOn(productService, 'getLastAvailableScene$')
      .and.returnValue(null);
    (timelineService as any)._handleUpdater$
      .subscribe((isLiveMode) => {
        if (isLiveMode) {
          expect(spyLoadingLatestScene).toHaveBeenCalled();
          expect((timelineService as any)._latestSceneSubscription$).not.toBe(null);
          return;
        }
        expect((timelineService as any)._latestSceneSubscription$).toBe(null);
      })
    timelineService.toggleLiveMode();
    timelineService.toggleLiveMode();
  });
});
