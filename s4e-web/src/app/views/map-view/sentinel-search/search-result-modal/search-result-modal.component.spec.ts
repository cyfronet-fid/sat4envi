import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchResultModalComponent } from './search-result-modal.component';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {makeModalProvider, MODAL_DEF} from '../../../../modal/modal.providers';
import {createModal} from '../../../../modal/state/modal.model';
import {ALERT_MODAL_ID, AlertModal} from '../../../../modal/components/alert-modal/alert-modal.model';
import {makeDetailsModal} from './search-result-modal.model';

describe('SearchResultModalComponent', () => {
  let component: SearchResultModalComponent;
  let fixture: ComponentFixture<SearchResultModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [
        {provide: MODAL_DEF, useValue: makeDetailsModal()}
      ],
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchResultModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
