/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {DynamicNotificationComponent} from './dynamic-notification.component';
import {NotificationsModule} from '../../notifications.module';
import {
  createGeneralNotification,
  Notification
} from '../../state/notification.model';
import {By} from '@angular/platform-browser';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';

describe('DynamicNotificationComponent', () => {
  let component: DynamicNotificationComponent;
  let fixture: ComponentFixture<DynamicNotificationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NotificationsModule.forRoot(), NoopAnimationsModule]
    }).compileComponents();
  });

  describe('without initialization', () => {
    beforeEach(() => {
      fixture = TestBed.createComponent(DynamicNotificationComponent);
      component = fixture.componentInstance;
    });

    it('should throw error if notification input is not set', () => {
      expect(() => {
        fixture.detectChanges();
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
      expect(
        fixture.debugElement.query(By.css('general-notification'))
      ).toBeTruthy();
    });

    it('should pass notification to created element', () => {
      expect(fixture.debugElement.children[0].componentInstance.notification).toBe(
        notification
      );
    });

    it('should hook to (activated)', () => {
      let spy = spyOn(component.activated, 'emit');
      fixture.debugElement.children[0].componentInstance.activated.emit();
      expect(spy).toHaveBeenCalledWith(undefined);
    });
  });
});
