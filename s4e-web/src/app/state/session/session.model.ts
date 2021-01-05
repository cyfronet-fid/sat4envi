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

export const COOKIE_POLICY_ACCEPTED_KEY = 'cookiePolicyAccepted';

export interface LoginFormState {
  email: string;
  password: string;
}

export interface Role {
  role: 'INST_ADMIN' | 'INST_MANAGER' | 'GROUP_MANAGER' | 'INST_MEMBER';
  institutionSlug: string | null;
  groupSlug: string | null;
}

export interface Session {
  email: string;
  name: string;
  surname: string;
  roles: Role[];
  authorities: string[];
  memberZK: boolean;
  admin: boolean;
  cookiePolicyAccepted: boolean;
}

export interface PasswordChangeFormState {
  oldPassword: string;
  newPassword: string;
}

/**
 * A factory function that creates Session
 */
export function createSession(
  params: Partial<Session> & {cookiePolicyAccepted: boolean}
): Session {
  return {
    cookiePolicyAccepted: params.cookiePolicyAccepted,
    email: params.email || null,
    roles: params.roles || [],
    authorities: params.authorities || [],
    name: '',
    surname: '',
    memberZK: false,
    admin: false
  };
}
