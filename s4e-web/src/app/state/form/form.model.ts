import {RegisterFormState} from '../../views/register/state/register.model';
import {LoginFormState, PasswordChangeFormState} from '../session/session.model';
import {SaveConfigForm} from '../../views/map-view/zk/save-config-modal/save-config-modal.model';
import {ReportForm} from '../../views/map-view/zk/report-modal/report-modal.model';
import {ShareConfigurationForm} from '../../views/map-view/zk/configuration/state/configuration.model';
import {Institution} from 'src/app/views/settings/state/institution/institution.model';
import { InvitationForm } from 'src/app/views/settings/people/invitation-form/invitation-form.model';


export interface FormState {
  login: LoginFormState;
  register: RegisterFormState;
  report: ReportForm;
  saveConfig: SaveConfigForm;
  configurationShare: ShareConfigurationForm;
  resetPassword: PasswordChangeFormState;
  addInstitution: Institution;
  invitation: InvitationForm;
}
