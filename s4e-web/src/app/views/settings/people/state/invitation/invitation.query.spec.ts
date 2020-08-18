import { InvitationStore } from './invitation.store';
import { InvitationQuery } from './invitation.query';

describe('PersonQuery', () => {
  let query: InvitationQuery;

  beforeEach(() => {
    query = new InvitationQuery(new InvitationStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
