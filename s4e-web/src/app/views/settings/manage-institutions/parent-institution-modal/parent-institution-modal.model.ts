/*
 * Copyright 2020 ACC Cyfronet AGH
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

import {ModalWithReturnValue, Modal} from '../../../../modal/state/modal.model';
import { Institution } from '../../state/institution/institution.model';

export const PARENT_INSTITUTION_MODAL_ID = 'parent-institution-modal';

export interface ParentInstitutionForm {
  searchInstitution: string;
}

export interface ParentInstitutionModal extends ModalWithReturnValue<Partial<Institution>> {
  selectedInstitution: Partial<Institution>;
}

export function isParentInstitutionModal(modal: Modal): modal is ParentInstitutionModal {
  return modal.id === PARENT_INSTITUTION_MODAL_ID;
}
