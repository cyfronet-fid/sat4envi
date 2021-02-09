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
import {MapData} from '../../state/map/map.model';
import {Legend} from '../../state/legend/legend.model';

export const REPORT_MODAL_ID = 'report-modal';

export interface ReportForm {
  caption: string;
  notes: string;
}

export interface ReportModal extends Modal {
  image: MapData;
  sceneDate: string;
  productName: string;
  legend: Legend | null;
}

export function isReportModal(modal: Modal): modal is ReportModal {
  return modal.id === REPORT_MODAL_ID && (modal as ReportModal).image != null;
}
