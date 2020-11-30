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
