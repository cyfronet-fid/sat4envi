import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {DynamicNotificationComponent} from './dynamic-notification.component';
import {NotificationsModule} from '../../notifications.module';
import {createGeneralNotification, Notification} from '../../state/notification.model';

describe('DynamicNotificationComponent', () => {
  let component: DynamicNotificationComponent;
  let fixture: ComponentFixture<DynamicNotificationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [NotificationsModule.forRoot()],
    })
      .compileComponents();
  }));

  describe('without initialization', () => {
    beforeEach(() => {
      fixture = TestBed.createComponent(DynamicNotificationComponent);
      component = fixture.componentInstance;
    });

    it('should throw error if notification input is not set', () => {
      expect(() => {
        fixture.detectChanges()
      }).toThrow('dynamic-notification requires [notification] to be defined');
    });
  });

  describe('with initialization', () => {
    let notification: Notification;

    beforeEach(() => {
      fixture = TestBed.createComponent(DynamicNotificationComponent);
      component = fixture.componentInstance;
      notification = createGeneralNotification({});
      component.notification = notification;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should create component based on notification', () => {
      expect(fixture.debugElement.children[0].name).toBe('general-notification')
    });

    it('should pass notification to created element', () => {
      expect(fixture.debugElement.children[0].componentInstance.notification).toBe(notification)
    });

    it('should hook to (activated)', () => {
      let spy = spyOn(component.activated, 'emit');
      fixture.debugElement.children[0].componentInstance.activated.emit();
      expect(spy).toHaveBeenCalledWith(undefined)
    });
  });
});
