import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {LegendDesignerComponent} from './legend-designer.component';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';

describe('LegendDesignerComponent', () => {
  let component: LegendDesignerComponent;
  let fixture: ComponentFixture<LegendDesignerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [MapModule, RouterTestingModule, HttpClientTestingModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LegendDesignerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
