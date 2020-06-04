export interface Institution {
  id: string;
  slug: string;
  name: string;
}

/**
 * A factory function that creates Institution
 */
export function createInstitution(params: Partial<Institution>) {
  return {

  } as Institution;
}
