export interface Institution {
  // unique ID
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
