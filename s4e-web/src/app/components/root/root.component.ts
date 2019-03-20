import {Component} from '@angular/core';
import {resetStores} from '@datorama/akita';
import {environment} from '../../../environments/environment';

@Component({
  selector: 's4e-root',
  templateUrl: './root.component.html',
  styleUrls: ['./root.component.scss']
})
export class RootComponent {
  PRODUCTION: boolean = environment.production;

  /**
   * THIS IS ONLY FOR NON PRODUCTION PURPOSES
   */
  devRefreshState() {
    resetStores();
    location.reload();
  }
}
