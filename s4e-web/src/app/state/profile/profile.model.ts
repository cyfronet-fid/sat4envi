import {Role} from '../session/session.model';

export interface Profile {
  email: string;
  name: string;
  surname: string;
  roles: Role[];
  memberZK: boolean;
  admin: boolean;
}

export interface ProfileState extends Profile {
  loggedIn: boolean;
}

export function createInitialState(): ProfileState {
  return {
    email: '',
    name: '',
    loggedIn: false,
    roles: [],
    surname: '',
    memberZK: false,
    admin: false
  };
}

export interface PasswordChangeFormState {
  oldPassword: string;
  newPassword: string;
}
