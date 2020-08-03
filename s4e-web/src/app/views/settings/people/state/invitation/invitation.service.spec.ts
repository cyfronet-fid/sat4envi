import { TestBed } from '@angular/core/testing';

import { InvitationService } from './invitation.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('InvitationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
      ]
    });
    TestBed.configureTestingModule({})
  });

  it('should be created', () => {
    const service: InvitationService = TestBed.get(InvitationService);
    expect(service).toBeTruthy();
  });
});
