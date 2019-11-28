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

  /**
   * THIS IS ONLY FOR NON PRODUCTION PURPOSES, UI design
   * :TODO after properly styling ModalModule this method should be removed
   */
  async modalDemo() {
    if (await this.modalService.confirm('Confirm Modal', 'Jeśli naciśniesz OK zobaczysz ALERT modal')) {
      await this.modalService.alert('Alert Modal', 'Po kliknięciu OK zobaczysz generyczny modal');
      this.modalService.show({id: DUMMY_MODAL_ID});
    }
  }
}
