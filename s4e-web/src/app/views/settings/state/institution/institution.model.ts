export interface Institution {
  id: string;

  parentName: string|null;
  parentSlug: string|null;

  slug: string;
  name: string;
  address: string;
  postalCode: string;
  city: string;
  phone: string;
  secondaryPhone: string;
  emblem: string;
  institutionAdminEmail: string;
}

/**
 * A factory function that creates Institution
 */
export function createInstitution(params: Partial<Institution>) {
  return {} as Institution;
}
