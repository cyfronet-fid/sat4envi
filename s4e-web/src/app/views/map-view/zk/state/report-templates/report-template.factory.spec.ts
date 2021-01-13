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

import * as Factory from 'factory.ts';
import {ReportTemplate} from './report-template.model';

export const ReportTemplateFactory = Factory.makeFactory<ReportTemplate>({
  uuid: Factory.each(i => `${i}`),
  caption: Factory.each(i => `Report template #${i}`),
  notes: Factory.each(i => `Note #${i}`),
  overlayIds: Factory.each(i => [i]),
  createdAt: null,
  productId: Factory.each(i => i)
});
