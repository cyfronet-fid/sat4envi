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

import {EntityState} from '@datorama/akita';
import {Base64Image} from '../../../../../common/types';
import {Modal} from '../../../../../modal/state/modal.model';

export const SHARE_CONFIGURATION_MODAL_ID = 'share-configuration-modal';

export interface ShareConfigurationForm {
  emails: string;
  caption: string;
  description: string;
}

export interface ShareConfigurationRequest {
  emails: string[];
  path: string;
  caption: string;
  description: string;
  thumbnail: string;
}

export interface ConfigurationModal extends Modal {
  mapImage: Base64Image;
  configurationUrl: string;
}

export function isConfigurationModal(modal: Modal): modal is ConfigurationModal {
  return modal.id === SHARE_CONFIGURATION_MODAL_ID
    && (modal as ConfigurationModal).mapImage != null
    && (modal as ConfigurationModal).configurationUrl != null;
}


export interface Configuration {

}

export interface ConfigurationState extends EntityState<Configuration> {}
