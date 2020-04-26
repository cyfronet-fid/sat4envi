import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NotificationService} from './notification.service';
import {NotificationStore} from './notification.store';
import {NotificationsModule} from '../notifications.module';
import {NotificationQuery} from './notification.query';
import {createGeneralNotification, createNotification} from './notification.model';

describe('NotificationService', () => {
  let service: NotificationService;
  let store: NotificationStore;
  let query: NotificationQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NotificationsModule.forRoot()],
    });

    query = TestBed.get(NotificationQuery);
    service = TestBed.get(NotificationService);
    store = TestBed.get(NotificationStore);
  });

  it('add', fakeAsync(() => {
    const notification = createNotification({duration: 1});
    service.add(notification);
    tick(1000);
    expect(query.getAll().length).toBe(0);
  }));

  it('remove', () => {
    const notification = createNotification();
    store.add(notification);
    service.remove(notification.id);
    expect(query.getAll().length).toBe(0);
  });

  it('addGeneral', () => {
    spyOn(service, 'add');
    service.addGeneral({id: '1234'});
    expect(service.add).toHaveBeenCalledWith(createGeneralNotification({id: '1234'}));
  });
});
