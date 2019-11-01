import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SearchResultsComponent} from './search-results.component';
import {ShareModule} from '../../../common/share.module';
import {TestingConfigProvider} from '../../../app.configuration.spec';

describe('SearchResultsComponent', () => {
  let component: SearchResultsComponent;
  let fixture: ComponentFixture<SearchResultsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [TestingConfigProvider],
      imports: [ShareModule],
      declarations: [SearchResultsComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
