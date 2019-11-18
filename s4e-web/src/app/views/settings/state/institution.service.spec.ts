import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { InstitutionService } from './institution.service';
import { InstitutionStore } from './institution.store';
import {TestingConfigProvider} from '../../../app.configuration.spec';
import {RouterTestingModule} from '@angular/router/testing';

describe('InstitutionService', () => {
  let institutionService: InstitutionService;
  let institutionStore: InstitutionStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InstitutionService, InstitutionStore, TestingConfigProvider],
      imports: [ HttpClientTestingModule, RouterTestingModule ],
    });

    institutionService = TestBed.get(InstitutionService);
    institutionStore = TestBed.get(InstitutionStore);
  });

  it('should be created', () => {
    expect(institutionService).toBeDefined();
  });

});
