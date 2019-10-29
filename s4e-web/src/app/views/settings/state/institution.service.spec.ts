import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { InstitutionService } from './institution.service';
import { InstitutionStore } from './institution.store';

describe('InstitutionService', () => {
  let institutionService: InstitutionService;
  let institutionStore: InstitutionStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InstitutionService, InstitutionStore],
      imports: [ HttpClientTestingModule ]
    });

    institutionService = TestBed.get(InstitutionService);
    institutionStore = TestBed.get(InstitutionStore);
  });

  it('should be created', () => {
    expect(institutionService).toBeDefined();
  });

});
