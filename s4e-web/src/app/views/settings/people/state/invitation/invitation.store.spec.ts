import { InvitationStore } from './invitation.store';

describe('InvitationStore', () => {
  let store: InvitationStore;

  beforeEach(() => {
    store = new InvitationStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
