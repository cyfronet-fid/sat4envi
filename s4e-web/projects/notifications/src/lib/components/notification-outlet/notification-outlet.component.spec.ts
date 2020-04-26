import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NotificationOutletComponent} from './notification-outlet.component';
import {NotificationStore} from '../../state/notification.store';
import {createGeneralNotification, Notification} from '../../state/notification.model';
import {By} from '@angular/platform-browser';
import {DynamicNotificationComponent} from '../dynamic-notification/dynamic-notification.component';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {NotificationsModule} from '../../notifications.module';

describe('NotificationOutletComponent', () => {
  let component: NotificationOutletComponent;
  let fixture: ComponentFixture<NotificationOutletComponent>;
  let store: NotificationStore;

  function expectNotificationCountToBe(expectedCount: number) {
    expect(fixture.debugElement.queryAll(By.css('ul.outlet > li')).length)
      .toBe(expectedCount);
  }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        NotificationsModule.forRoot()
      ],
    }).compileComponents();
    store = TestBed.get(NotificationStore);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NotificationOutletComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display notifications', () => {
    const notification = createGeneralNotification({});
    expectNotificationCountToBe(0);
    store.add(notification);
    fixture.detectChanges();
    expectNotificationCountToBe(1);
    store.remove(notification.id);
    fixture.detectChanges();
    expectNotificationCountToBe(0);
  });

  describe('single notification', () => {
    let notification: Notification;
    let notificationComponent: DynamicNotificationComponent;
    beforeEach(() => {
      notification = createGeneralNotification({});
      store.add(notification);
      fixture.detectChanges();
      notificationComponent = notificationComponent = fixture.debugElement.query(By.css('ul.outlet > li > dynamic-notification')).componentInstance;
    });

    it('should pass inputs to notification', () => {
      expect((notificationComponent as any)._notification).toBe(notification);
    });

    it('should call activated', () => {
      const ID = '1234';
      const spy = spyOn(component, 'activated').and.stub();
      notificationComponent.activated.emit();
      expect(spy).toHaveBeenCalledWith(notification.id, undefined)
    });

    it('should call activated with ctx', () => {
      const ID = '1234';
      const ctx = 'ctx';
      const spy = spyOn(component, 'activated').and.stub();
      notificationComponent.activated.emit(ctx);
      expect(spy).toHaveBeenCalledWith(notification.id, ctx)
    });
  });
});
