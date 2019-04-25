import { TestBed, inject } from '@angular/core/testing';
import { IsLoggedIn } from './auth-guard.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {SessionQuery} from '../../state/session/session.query';

describe('IsLoggedIn', () => {
  let service: IsLoggedIn;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      providers: [IsLoggedIn, SessionQuery]
    });
    service = TestBed.get(IsLoggedIn);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
