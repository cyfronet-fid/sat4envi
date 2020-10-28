import { Person } from './../person/person.model';

export interface InvitationResendRequest {
  oldEmail: string;
  newEmail: string;
  forAdmin: boolean;
}

type stateType = 'waiting' | `rejected`;
export interface Invitation {
  id: string;
  email: string;
  status: stateType;
}

const invitationName = 'Invitation';
export function isInvitation(item: any) {
  return item.roles.length === 0
    && item.name === invitationName
    && !!item.surname
    && !!item.id;
}

export function invitationToPerson(invitation: Invitation): Person {
  return {
    email: invitation.email,
    name: invitationName,
    surname: `(${invitation.status})`,
    roles: [],
    id: invitation.id
  } as any as Person;
}
