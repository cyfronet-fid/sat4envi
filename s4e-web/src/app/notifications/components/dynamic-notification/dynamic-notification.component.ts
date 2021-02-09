import {
  Component,
  ComponentFactoryResolver,
  ComponentRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import {Notification} from '../../state/notification.model';
import {NotificationMapperService} from '../../utils/notification-mapper.service';
import {NotificationComponent} from '../notification.component';
import {Subscription} from 'rxjs';

@Component({
  selector: 'dynamic-notification',
  templateUrl: './dynamic-notification.component.html',
  styleUrls: ['./dynamic-notification.component.scss']
})
export class DynamicNotificationComponent implements OnInit, OnDestroy {
  @Output() activated = new EventEmitter<string | void>();
  @ViewChild('container', {read: ViewContainerRef, static: true})
  container: ViewContainerRef;
  private componentRef: ComponentRef<any>;
  private sub: Subscription | null = null;

  constructor(
    private mapperService: NotificationMapperService,
    protected componentFactoryResolver: ComponentFactoryResolver
  ) {}

  protected _notification: Notification = null;

  @Input() set notification(notification: Notification) {
    if (this._notification === notification) {
      return;
    }
    this._notification = notification;
    const component = this.mapperService.clazzToComponent(notification.clazz);
    const factory = this.componentFactoryResolver.resolveComponentFactory(component);
    this.componentRef = this.container.createComponent(factory);

    if (this.componentRef == null) {
      throw Error(
        `${component.toString()} was not created. Did you add it to 'entryComponents'?`
      );
    }

    (this.componentRef
      .instance as NotificationComponent<any>).notification = notification;
    this.sub = (this.componentRef
      .instance as NotificationComponent<any>).activated.subscribe((ctx: string) =>
      this.activated.emit(ctx)
    );
  }

  ngOnInit() {
    if (!this._notification) {
      throw Error('dynamic-notification requires [notification] to be defined');
    }
  }

  ngOnDestroy() {
    if (this.sub) {
      this.sub.unsubscribe();
    }

    /**
     * :TODO: THIS IS HACK, as for angular version 5 ngOnDestroy is called before animations
     * have chance to finish, this hack postpones it for 5 seconds, which is a reasonable time
     * for any UI animation to finish. This code should be rewritten after bug is fixed by angular
     * team
     */
    setTimeout(() => {
      if (this.componentRef) {
        this.componentRef.destroy();
        this.componentRef = null;
      }
    }, 5000);
  }
}
