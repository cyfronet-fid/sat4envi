import {Component, Inject, OnInit} from '@angular/core';
import {FormState} from '../../../../state/form/form.model';
import {ModalService} from '../../../../modal/state/modal.service';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {Modal} from '../../../../modal/state/modal.model';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {isListConfigsModal, LIST_CONFIGS_MODAL_ID} from './list-configs-modal.model';
import {ModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ViewConfigurationService} from '../../state/view-configuration/view-configuration.service';
import {ViewConfigurationQuery} from '../../state/view-configuration/view-configuration.query';
import {ViewConfiguration, ViewConfigurationEx} from '../../state/view-configuration/view-configuration.model';
import {Observable} from 'rxjs';
import {MapService} from '../../state/map/map.service';

@Component({
  selector: 's4e-list-configs-modal',
  templateUrl: './list-configs-modal.component.html',
  styleUrls: ['./list-configs-modal.component.scss']
})
export class ListConfigsModalComponent extends ModalComponent implements OnInit {
  isWorking: boolean = false;
  loading$: Observable<boolean>;
  errorMsg: string;
  private configs$: Observable<ViewConfigurationEx[]>;

  constructor(modalService: ModalService,
              @Inject(MODAL_DEF) modal: Modal,
              private modalQuery: ModalQuery,
              protected fm: AkitaNgFormsManager<FormState>,
              private configurationService: ViewConfigurationService,
              private configurationQuery: ViewConfigurationQuery,
              private mapService: MapService) {
    super(modalService, modal.id);

    if (!isListConfigsModal(modal)) {
      throw new Error(`${modal} is not a valid ${LIST_CONFIGS_MODAL_ID}`);
    }
  }

  ngOnInit(): void {
    this.configurationService.get();
    this.configs$ = this.configurationQuery.selectAllAsEx();
    this.loading$ = this.configurationQuery.selectLoading();
  }

  async deleteConfig(config: ViewConfiguration) {
    if (await this.modalService.confirm('Potwierdź operacje', `Na pewno usunąć konfiguracje ${config.caption}`)) {
      this.configurationService.delete(config.uuid);
    }
  }

  loadConfig(configuration: ViewConfiguration) {
    this.mapService.updateStoreByView(configuration.configuration);
    this.dismiss();
  }

  sendTo(configuration: ViewConfiguration) {
    console.log(`SendTo configuration ${configuration.uuid}`);
  }
}
