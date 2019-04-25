import {RegisterFormState} from '../../views/register/state/register.model';
import {LoginFormState} from '../session/session.model';

export interface FormState {
  login: LoginFormState;
  register: RegisterFormState;
}
