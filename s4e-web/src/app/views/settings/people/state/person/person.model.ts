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

export const DEFAULT_GROUP_NAME = '__default__';
export const DEFAULT_GROUP_SLUG = 'default';

export interface UserRole {
  institutionSlug: string;
  role: 'INST_MEMBER' | 'INST_ADMIN';
}

export interface Person {
  id: string;
  email: string;
  name: string;
  surname: string;
  roles: UserRole[];
  isAdmin: boolean;
  hasGrantedDeleteInstitution: boolean;
}

/**
 * A factory function that creates Person
 */
export function createPerson(params: Partial<Person>) {
  return {

  } as Person;
}
