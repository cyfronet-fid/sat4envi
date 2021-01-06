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
import {switchMap} from 'rxjs/operators';

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
    this.mapService.updateStoreByView(configuration.configuration).subscribe();
    this.dismiss();
  }

  sendTo(configuration: ViewConfiguration) {
    this.mapService.updateStoreByView(configuration.configuration).subscribe(() => this.configurationService.setActive(configuration.uuid));
    this.dismiss();
  }
}
