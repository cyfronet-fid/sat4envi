import {NotificationQuery} from './notification.query';
import {TestBed} from '@angular/core/testing';
import {NotificationsModule} from '../notifications.module';

describe('NotificationQuery', () => {
  let query: NotificationQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NotificationsModule.forRoot()],
    });

    query = TestBed.get(NotificationQuery);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });
});
