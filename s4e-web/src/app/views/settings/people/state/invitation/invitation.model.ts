import { Person } from '../person/person.model';

type stateType = 'waiting' | `rejected`;
export interface Invitation {
  email: string;
  status: stateType;
  token: string;
}

const invitationName = 'Invitation';
export function isInvitation(item: any) {
  return item.roles.length === 0
    && item.name === invitationName
    && !!item.surname
    && !!item.token;
}
export class InvitationToPersonAdapter {
  private _email: string;
  private _state: stateType;
  private _token: string;

  email(email: string) {
    this._email = email;
    return this;
  }

  status(state: stateType) {
    this._state = state;
    return this;
  }

  token(token: string) {
    this._token = token;
    return this;
  }

  build(): any {
    return {
      email: this._email,
      name: invitationName,
      surname: `(${this._state})`,
      roles: [],
      token: this._token,
    } as any as Person;
  }
}
