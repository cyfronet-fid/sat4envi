import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MapModule} from '../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ProductService} from '../state/product/product.service';
import {ProductStore} from '../state/product/product.store';
import {ProductQuery} from '../state/product/product.query';
import {ViewManagerComponent} from './view-manager.component';
import {ProductFactory} from '../state/product/product.factory.spec';
import {LocalStorageTestingProvider} from '../../../app.configuration.spec';
import {TimelineService} from '../state/scene/timeline.service';
import {of} from 'rxjs';

describe('ViewManagerComponent', () => {
  let fixture: ComponentFixture<ViewManagerComponent>;
  let component: ViewManagerComponent;
  let productQuery: ProductQuery;
  let productStore: ProductStore;
  let productService: ProductService;
  let timelineService: TimelineService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [MapModule, RouterTestingModule, HttpClientTestingModule]
    })
      .compileComponents();
    productQuery = TestBed.get(ProductQuery);
    productStore = TestBed.get(ProductStore);
    productService = TestBed.get(ProductService);
    timelineService = TestBed.get(TimelineService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('selectProduct should set product', async () => {
    const product = ProductFactory.build();
    productStore.set([product]);
    const spy = spyOn(productService, 'setActive');
    spyOn(timelineService, 'confirmTurningOfLiveMode')
      .and.returnValue(of(true).toPromise());
    await component.selectProduct(product.id);
    expect(spy).toHaveBeenCalledWith(product.id);
  });

  it('selectProduct should unset product if active one is selected', async () => {
    const product = ProductFactory.build();
    productStore.set([product]);
    productStore.setActive(product.id);
    const spy = spyOn(productService, 'setActive');
    spyOn(timelineService, 'confirmTurningOfLiveMode')
      .and.returnValue(of(true).toPromise());
    await component.selectProduct(product.id);
    expect(spy).toHaveBeenCalledWith(null);
  });
});
