import {ProfileQuery} from './profile.query';
import {async, TestBed} from '@angular/core/testing';
import {CommonStateModule} from '../common-state.module';
import {take} from 'rxjs/operators';
import {ProfileStore} from './profile.store';

describe('ProfileQuery', () => {
  let query: ProfileQuery;
  let store: ProfileStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [CommonStateModule],
    });

    query = TestBed.get(ProfileQuery);
    store = TestBed.get(ProfileStore);
  }));

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('should selectMemberZK', () => {
    const spy = spyOn(query, 'select').and.returnValue(true);
    query.selectMemberZK();
    expect(spy).toHaveBeenCalledWith('memberZK');
  });

  function canSeeInstitutions$(): Promise<boolean> {
    return query.selectCanSeeInstitutions().pipe(take(1)).toPromise();
  }

  it('should resolve to true if user is admin', async () => {
    store.update({admin: true});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to true if user has INST_ADMIN role in group', async () => {
    store.update({roles: [{groupSlug: 'gr', institutionSlug: 'inst', role: 'INST_ADMIN'}]});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to true if user has GROUP_MANAGER role in group', async () => {
    store.update({roles: [{groupSlug: 'gr', institutionSlug: 'inst', role: 'GROUP_MANAGER'}]});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to true if user has INST_MANAGER role in group', async () => {
    store.update({roles: [{groupSlug: 'gr', institutionSlug: 'inst', role: 'INST_MANAGER'}]});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to false if user has no managerial role', async () => {
    store.update({roles: [{groupSlug: 'gr', institutionSlug: 'inst', role: 'GROUP_MEMBER'}]});
    expect(await canSeeInstitutions$()).toBeFalsy();
  });

  it('should not resolve to true if user has at least one managerial role in group', async () => {
    store.update({
      roles: [
        {groupSlug: 'gr', institutionSlug: 'inst', role: 'GROUP_MEMBER'},
        {groupSlug: 'gr2', institutionSlug: 'inst2', role: 'GROUP_MANAGER'}
      ]
    });
    expect(await canSeeInstitutions$()).toBeTruthy();
  });
});
