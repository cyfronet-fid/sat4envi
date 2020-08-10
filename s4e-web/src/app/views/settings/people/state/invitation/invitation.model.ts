import { Person } from './../person/person.model';

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

export function invitationToPerson(invitation: Invitation): Person {
  return {
    email: invitation.email,
    name: invitationName,
    surname: `(${invitation.status})`,
    roles: [],
    token: invitation.token
  } as any as Person;
}
