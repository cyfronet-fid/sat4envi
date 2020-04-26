import {Component} from '@angular/core';
import {resetStores} from '@datorama/akita';
import {environment} from '../../../environments/environment';
import {ModalService} from '../../modal/state/modal.service';
import {NotificationService} from 'notifications';

@Component({
  selector: 's4e-root',
  templateUrl: './root.component.html',
  styleUrls: ['./root.component.scss']
})
export class RootComponent {
  PRODUCTION: boolean = environment.production;

  constructor(private modalService: ModalService, private notificationService: NotificationService) {
  }

  /**
   * THIS IS ONLY FOR NON PRODUCTION PURPOSES
   */
  devRefreshState() {
    resetStores();
    location.reload();
  }

  devShowNotifications() {
    this.notificationService.addGeneral({
      type: 'info',
      content: 'hello'
    });
    this.notificationService.addGeneral({
      type: 'warning',
      content: 'hello'
    });
    this.notificationService.addGeneral({
      type: 'success',
      content: 'hello'
    });
    this.notificationService.addGeneral({
      type: 'error',
      content: 'hello'
    });
  }
}

