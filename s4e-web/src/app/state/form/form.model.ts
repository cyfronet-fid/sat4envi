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

import { ExpertHelpForm } from './../../views/map-view/zk/expert-help-modal/expert-help-modal.model';
import {RegisterFormState} from '../../views/register/state/register.model';
import {LoginFormState, PasswordChangeFormState} from '../session/session.model';
import {SaveConfigForm} from '../../views/map-view/zk/save-config-modal/save-config-modal.model';
import {ReportForm} from '../../views/map-view/zk/report-modal/report-modal.model';
import {ShareConfigurationForm} from '../../views/map-view/zk/configuration/state/configuration.model';
import {Institution} from 'src/app/views/settings/state/institution/institution.model';
import { InvitationForm } from 'src/app/views/settings/people/invitation-form/invitation-form.model';
import { JwtTokenForm } from 'src/app/views/map-view/jwt-token-modal/jwt-token-modal.model';


export interface FormState {
  login: LoginFormState;
  register: RegisterFormState;
  report: ReportForm;
  saveConfig: SaveConfigForm;
  configurationShare: ShareConfigurationForm;
  resetPassword: PasswordChangeFormState;
  addInstitution: Institution;
  invitation: InvitationForm;
  jwtToken: JwtTokenForm;
  expertHelp: ExpertHelpForm;
  removeUser: {password: string};
}
