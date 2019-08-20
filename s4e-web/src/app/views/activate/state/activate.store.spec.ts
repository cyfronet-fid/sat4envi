import {ActivateStore} from './activate.store';
import {ActivateQuery} from './activate.query';

describe('ActivateStore', () => {
  let store: ActivateStore;

  beforeEach(() => {
    store = new ActivateStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

  it('should update state', () => {
    store.setState('resending');
    const query = new ActivateQuery(store);
    expect(query.getValue().state).toBe('resending');
  });
});
