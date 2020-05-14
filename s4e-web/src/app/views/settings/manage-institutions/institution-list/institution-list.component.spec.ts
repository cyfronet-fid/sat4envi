import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InstitutionListComponent } from './institution-list.component';
import {ManageInstitutionsModule} from '../manage-institutions.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';

describe('InstitutionListComponent', () => {
  let component: InstitutionListComponent;
  let fixture: ComponentFixture<InstitutionListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ManageInstitutionsModule, HttpClientTestingModule, RouterTestingModule],
      providers: [TestingConfigProvider]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InstitutionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
