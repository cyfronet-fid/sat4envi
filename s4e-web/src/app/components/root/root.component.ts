import {Component} from '@angular/core';
import {resetStores} from '@datorama/akita';
import {environment} from '../../../environments/environment';
import {ModalService} from '../../modal/state/modal.service';
import {DUMMY_MODAL_ID} from '../../modal/components/dummy-modal/dummy-modal.model';

@Component({
  selector: 's4e-root',
  templateUrl: './root.component.html',
  styleUrls: ['./root.component.scss']
})
export class RootComponent {
  PRODUCTION: boolean = environment.production;

  constructor(private modalService: ModalService) {
  }

  /**
   * THIS IS ONLY FOR NON PRODUCTION PURPOSES
   */
  devRefreshState() {
    resetStores();
    location.reload();
  }
}

