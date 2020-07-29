export const DEFAULT_GROUP_NAME = '__default__';
export const DEFAULT_GROUP_SLUG = 'default';

export interface Person {
  // this is ID
  email: string;
  name: string;
  surname: string;
  roles: any[],
  // createDate: string
}

/**
 * A factory function that creates Person
 */
export function createPerson(params: Partial<Person>) {
  return {

  } as Person;
}
