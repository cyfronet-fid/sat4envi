import {TestBed} from '@angular/core/testing';
import {NotificationMapperService} from './notification-mapper.service';
import {NotificationsModule} from '../notifications.module';
import {Component} from '@angular/core';
import {DefaultNotificationClazz, NotificationClazz} from '../notifications.tokens';
import {GeneralNotificationComponent} from '../components/general-notification/general-notification.component';

@Component({
  selector: 'mock-notification',
  template: '<div></div>',
  styleUrls: []
})
export class MockNotificationComponent {}

describe('NotificationMapperService', () => {
  let service: NotificationMapperService;

  describe('Standard providers', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [NotificationsModule.forRoot()],
        providers: [
          {
            provide: NotificationClazz,
            useValue: {
              name: 'MockNotificationComponent',
              component: MockNotificationComponent
            },
            multi: true
          }
        ]
      });
      service = TestBed.inject(NotificationMapperService);
    });

    it('clazzToComponent should work', () => {
      expect(service.clazzToComponent('MockNotificationComponent')).toBe(
        MockNotificationComponent
      );
    });

    it('clazzToComponent should give default class', () => {
      expect(service.clazzToComponent('NonExistentClass')).toBe(
        GeneralNotificationComponent
      );
    });
  });

  describe('Overwritten default notification', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [NotificationsModule.forRoot()],
        providers: [
          {provide: DefaultNotificationClazz, useValue: MockNotificationComponent}
        ]
      });
      service = TestBed.inject(NotificationMapperService);
    });

    it('clazzToComponent should give default class', () => {
      expect(service.clazzToComponent('NonExistentClass')).toBe(
        MockNotificationComponent
      );
    });
  });
});
