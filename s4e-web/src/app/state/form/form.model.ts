import {RegisterFormState} from '../../views/register/state/register.model';
import {LoginFormState} from '../session/session.model';
import {PersonForm} from '../../views/settings/people/state/person.model';
import {GroupForm} from '../../views/settings/groups/state/group.model';
import {ReportForm} from '../../views/map-view/zk/report-modal/report-modal.model';
import {SentinelSearchForm} from '../../views/map-view/state/sentinel-search/sentinel-search.model';
import {ShareConfigurationForm} from '../../views/map-view/zk/configuration/state/configuration.model';

export interface FormState {
  login: LoginFormState;
  register: RegisterFormState;
  addPerson: PersonForm;
  addGroup: GroupForm;
  report: ReportForm;
  configurationShare: ShareConfigurationForm;
  sentinelSearch: SentinelSearchForm;
}
