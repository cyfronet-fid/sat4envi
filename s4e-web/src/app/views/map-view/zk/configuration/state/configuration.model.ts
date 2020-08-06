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
