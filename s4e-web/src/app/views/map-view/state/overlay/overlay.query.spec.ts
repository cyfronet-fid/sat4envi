import { OverlayQuery } from './overlay.query';
import { OverlayStore } from './overlay.store';

describe('OverlayQuery', () => {
  let query: OverlayQuery;

  beforeEach(() => {
    query = new OverlayQuery(new OverlayStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
