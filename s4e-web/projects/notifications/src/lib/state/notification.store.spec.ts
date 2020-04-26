import {NotificationStore} from './notification.store';
import {TestBed} from '@angular/core/testing';

describe('NotificationStore', () => {
  let store: NotificationStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NotificationStore]
    });
    store = TestBed.get(NotificationStore);
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });
});
