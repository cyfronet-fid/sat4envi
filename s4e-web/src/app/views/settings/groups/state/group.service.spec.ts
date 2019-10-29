import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { GroupService } from './group.service';
import { GroupStore } from './group.store';

describe('GroupService', () => {
  let groupService: GroupService;
  let groupStore: GroupStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GroupService, GroupStore],
      imports: [ HttpClientTestingModule ]
    });

    groupService = TestBed.get(GroupService);
    groupStore = TestBed.get(GroupStore);
  });

  it('should be created', () => {
    expect(groupService).toBeDefined();
  });

});
