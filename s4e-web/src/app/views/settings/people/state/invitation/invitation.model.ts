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

import {Person} from './../person/person.model';

export interface InvitationResendRequest {
  oldEmail: string;
  newEmail: string;
  forAdmin: boolean;
}

type stateType = 'waiting' | `rejected`;
const stateTypeLabel = {
  waiting: 'oczekujące',
  rejected: 'odrzucone'
};
export interface Invitation {
  id: string;
  email: string;
  status: stateType;
}

const invitationName = 'Zaproszenie';
export function isInvitation(item: any) {
  return (
    item.roles.length === 0 &&
    item.name === invitationName &&
    !!item.surname &&
    !!item.id
  );
}

export function invitationToPerson(invitation: Invitation): Person {
  return ({
    email: invitation.email,
    name: invitationName,
    surname: `(${stateTypeLabel[invitation.status.toLowerCase()]})`,
    roles: [],
    id: invitation.id
  } as any) as Person;
}
