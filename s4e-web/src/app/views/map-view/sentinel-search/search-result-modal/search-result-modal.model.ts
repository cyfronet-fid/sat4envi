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

import {createModal, Modal} from '../../../../modal/state/modal.model';
import {SentinelSearchResult} from '../../state/sentinel-search/sentinel-search.model';

export const SENTINEL_SEARCH_RESULT_MODAL_ID = 'search-result-modal-id';

export interface DetailsModal extends Modal {
  showNavigation: boolean;
  mode: 'sentinel' | 'scene';
  entity: SentinelSearchResult | null;
}

export function makeDetailsModal(showNavigation: boolean = true,
                                 mode: 'sentinel' | 'scene' = 'sentinel',
                                 entity: SentinelSearchResult | null = null): DetailsModal
{
  return {
    ...createModal({id: SENTINEL_SEARCH_RESULT_MODAL_ID, size: 'lg'}),
    showNavigation,
    mode,
    entity
  };
}
