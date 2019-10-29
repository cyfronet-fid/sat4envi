import {RegisterFormState} from '../../views/register/state/register.model';
import {LoginFormState} from '../session/session.model';
import {PersonForm} from '../../views/settings/people/state/person.model';
import {GroupForm} from '../../views/settings/groups/state/group.model';

export interface FormState {
  login: LoginFormState;
  register: RegisterFormState;
  addPerson: PersonForm;
  addGroup: GroupForm;
}
