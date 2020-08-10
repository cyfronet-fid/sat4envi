import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MapModule} from '../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MapViewComponent} from '../map-view.component';
import {ActivatedRoute} from '@angular/router';
import {ProductService} from '../state/product/product.service';
import {ProductStore} from '../state/product/product.store';
import {ProductQuery} from '../state/product/product.query';
import {ViewManagerComponent} from './view-manager.component';
import {ProductFactory} from '../state/product/product.factory.spec';

describe('ViewManagerComponent', () => {
  let fixture: ComponentFixture<ViewManagerComponent>;
  let component: ViewManagerComponent;
  let productQuery: ProductQuery;
  let productStore: ProductStore;
  let productService: ProductService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, RouterTestingModule, HttpClientTestingModule]
    })
      .compileComponents();
    productQuery = TestBed.get(ProductQuery);
    productStore = TestBed.get(ProductStore);
    productService = TestBed.get(ProductService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('selectProduct should set product', () => {
    const product = ProductFactory.build();
    productStore.set([product]);
    const spy = spyOn(productService, 'setActive');
    component.selectProduct(product.id);
    expect(spy).toHaveBeenCalledWith(product.id);
  });

  it('selectProduct should unset product if active one is selected', () => {
    const product = ProductFactory.build();
    productStore.set([product]);
    productStore.setActive(product.id);
    const spy = spyOn(productService, 'setActive');
    component.selectProduct(product.id);
    expect(spy).toHaveBeenCalledWith(null);
  });
});
