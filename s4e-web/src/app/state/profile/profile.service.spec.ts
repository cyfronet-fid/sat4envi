import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ProfileService } from './profile.service';
import { ProfileStore } from './profile.store';

describe('ProfileService', () => {
  let profileService: ProfileService;
  let profileStore: ProfileStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProfileService, ProfileStore],
      imports: [ HttpClientTestingModule ]
    });

    profileService = TestBed.get(ProfileService);
    profileStore = TestBed.get(ProfileStore);
  });

  it('should be created', () => {
    expect(profileService).toBeDefined();
  });

});
