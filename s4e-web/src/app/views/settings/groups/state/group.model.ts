export interface Group {
  slug: string;
  name: string;
}

export interface GroupForm {
  name: string;
  description: string;
  users: any // {_: string[]}
}

/**
 * A factory function that creates Group
 */
export function createGroup(params: Partial<Group>) {
  return {

  } as Group;
}
