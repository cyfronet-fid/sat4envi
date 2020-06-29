import { PasswordChangeFormState } from './../profile/profile.model';
import {RegisterFormState} from '../../views/register/state/register.model';
import {LoginFormState} from '../session/session.model';
import {PersonForm} from '../../views/settings/people/state/person.model';
import {GroupForm} from '../../views/settings/groups/state/group.model';
import {SaveConfigForm} from '../../views/map-view/zk/save-config-modal/save-config-modal.model';
import {ReportForm} from '../../views/map-view/zk/report-modal/report-modal.model';
import {ShareConfigurationForm} from '../../views/map-view/zk/configuration/state/configuration.model';
import { Institution } from 'src/app/views/settings/state/institution/institution.model';


export interface FormState {
  login: LoginFormState;
  register: RegisterFormState;
  addPerson: PersonForm;
  addGroup: GroupForm;
  report: ReportForm;
  saveConfig: SaveConfigForm;
  configurationShare: ShareConfigurationForm;
  resetPassword: PasswordChangeFormState;
  addInstitution: Institution;
}
