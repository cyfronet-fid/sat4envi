import { filter } from 'rxjs/operators';
import {Component} from '@angular/core';
import {resetStores} from '@datorama/akita';
import {environment} from '../../../environments/environment';
import {NotificationService} from 'notifications';
import { Router, NavigationEnd } from '@angular/router';
import { MapService } from 'src/app/views/map-view/state/map/map.service';
import { SearchResultsService } from 'src/app/views/map-view/state/location-search-results/locations-search-results.service';

@Component({
  selector: 's4e-root',
  templateUrl: './root.component.html',
  styleUrls: ['./root.component.scss']
})
export class RootComponent {
  PRODUCTION: boolean = environment.production;

  constructor(private notificationService: NotificationService) {
    console.log('SOK version ' + environment.version);
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

