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
