import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RegisterService } from './register.service';
import { RegisterStore } from './register.store';
import {RouterTestingModule} from '@angular/router/testing';

describe('RegisterService', () => {
  let registerService: RegisterService;
  let registerStore: RegisterStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RegisterService, RegisterStore],
      imports: [ HttpClientTestingModule, RouterTestingModule ]
    });

    registerService = TestBed.get(RegisterService);
    registerStore = TestBed.get(RegisterStore);
  });

  it('should be created', () => {
    expect(registerService).toBeDefined();
  });

});
