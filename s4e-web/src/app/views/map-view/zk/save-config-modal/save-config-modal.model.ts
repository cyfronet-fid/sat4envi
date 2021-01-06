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

import {Modal} from '../../../../modal/state/modal.model';
import {ViewConfigurationEx} from '../../state/view-configuration/view-configuration.model';

export const SAVE_CONFIG_MODAL_ID = 'save-config-modal';

export interface SaveConfigForm {
  configurationName: string;
}

export interface SaveConfigModal extends Modal {
  viewConfiguration : ViewConfigurationEx
}

export function isSaveConfigModal(modal: Modal): modal is SaveConfigModal {
  return modal.id == SAVE_CONFIG_MODAL_ID
    && (modal as SaveConfigModal).viewConfiguration != null;
}
