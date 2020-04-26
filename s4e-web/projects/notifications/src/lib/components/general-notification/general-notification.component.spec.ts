import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {GeneralNotificationComponent} from './general-notification.component';
import {By} from '@angular/platform-browser';
import {createGeneralNotification, GeneralNotification} from '../../state/notification.model';
import {NotificationsModule} from '../../notifications.module';

describe('GeneralNotificationComponent', () => {
  let component: GeneralNotificationComponent;
  let fixture: ComponentFixture<GeneralNotificationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [NotificationsModule.forRoot()]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GeneralNotificationComponent);
    component = fixture.componentInstance;
  });

  it('should throw error if notification input is not set', () => {
    expect(() => {
      fixture.detectChanges()
    }).toThrow('notification-component inheriting NotificationComponent must set [notification]');
  });

  describe('proper setup', () => {
    let notification: GeneralNotification;

    beforeEach(() => {
      notification = createGeneralNotification({content: 'sample notification content'});
      component.notification = notification;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    // There's no more button
    // it('should call activated when button is clicked', () => {
    //   let spy = spyOn(component.activated, 'emit');
    //   fixture.debugElement.query(By.css('.notification-action > button')).nativeElement.click();
    //   expect(spy).toHaveBeenCalled();
    // });

    it('should contain content', () => {
      expect(fixture.debugElement.nativeElement.textContent).toContain('sample notification content')
    });
  });

  describe('not clearable', () => {
    it('should ', () => {
      component.notification = createGeneralNotification({clearable: false});
      fixture.detectChanges();

      expect(fixture.debugElement.query(By.css('.notification-action'))).toBeNull();
    });
  });
});
