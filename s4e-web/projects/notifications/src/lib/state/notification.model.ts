import {EntityState, guid, ID} from '@datorama/akita';

export interface Notification {
  id: ID;
  clearable: boolean;
  duration: number;
  clazz: string|null;
  action: string;
}

export interface GeneralNotification extends Notification {
  content: string;
  actionCaption: string;
  ctx: any;
  type: 'info'|'warning'|'error'|'success';
}

/**
 * A factory function that creates Notification
 */
export function createNotification(params: Partial<Notification> = {}): Notification {
  return {
    id: guid(),
    clearable: true,
    duration: 5000,
    clazz: null,
    action: 'dismiss',
    ...params
  } as Notification;
}

export function createGeneralNotification(params: Partial<GeneralNotification>): GeneralNotification {
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
