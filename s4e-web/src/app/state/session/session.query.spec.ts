import {SessionQuery} from './session.query';
import {async, TestBed} from '@angular/core/testing';
import {CommonStateModule} from '../common-state.module';
import {take} from 'rxjs/operators';
import {SessionStore} from './session.store';
import {RoleFactory} from './session.factory.spec';

describe('SessionQuery', () => {
  let query: SessionQuery;
  let store: SessionStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [CommonStateModule],
    });

    query = TestBed.get(SessionQuery);
    store = TestBed.get(SessionStore);
  }));

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('should selectMemberZK', async () => {
    store.update({memberZK: false});
    expect(await query.selectMemberZK().pipe(take(1)).toPromise()).toBe(false);
    store.update({memberZK: true});
    expect(await query.selectMemberZK().pipe(take(1)).toPromise()).toBe(true);
  });

  function canSeeInstitutions$(): Promise<boolean> {
    return query.selectCanSeeInstitutions().pipe(take(1)).toPromise();
  }

  it('should resolve to true if user is admin', async () => {
    store.update({admin: true});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to true if user has INST_ADMIN role in group', async () => {
    store.update({roles: [RoleFactory.build({role: 'INST_ADMIN'})]});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to true if user has GROUP_MANAGER role in group', async () => {
    store.update({roles: [RoleFactory.build({role: 'GROUP_MANAGER'})]});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to true if user has INST_MANAGER role in group', async () => {
    store.update({roles: [RoleFactory.build({role: 'INST_MANAGER'})]});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to false if user has no managerial role', async () => {
    store.update({roles: [RoleFactory.build({role: 'GROUP_MEMBER'})]});
    expect(await canSeeInstitutions$()).toBeFalsy();
  });

  it('should not resolve to true if user has at least one managerial role in group', async () => {
    store.update({
      roles: [
        RoleFactory.build({role: 'GROUP_MEMBER'}),
        RoleFactory.build({role: 'GROUP_MANAGER'})
      ]
    });
    expect(await canSeeInstitutions$()).toBeTruthy();
  });
});
