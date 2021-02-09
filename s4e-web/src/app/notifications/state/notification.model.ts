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

import {EntityState, guid, ID} from '@datorama/akita';

export interface Notification {
  id: ID;
  clearable: boolean;
  duration: number;
  clazz: string | null;
  action: string;
}

export interface GeneralNotification extends Notification {
  content: string;
  actionCaption: string;
  ctx: any;
  type: 'info' | 'warning' | 'error' | 'success';
}

/**
 * A factory function that creates Notification
 */
export function createNotification(
  params: Partial<Notification> = {}
): Notification {
  return {
    id: guid(),
    clearable: true,
    duration: 5000,
    clazz: null,
    action: 'dismiss',
    ...params
  } as Notification;
}

export function createGeneralNotification(
  params: Partial<GeneralNotification>
): GeneralNotification {
  return {
    ...createNotification(params),
    content: '',
    type: 'info',
    actionCaption: 'Dismiss',
    ctx: null,
    ...params
  };
}

export interface NotificationState extends EntityState<Notification> {}
