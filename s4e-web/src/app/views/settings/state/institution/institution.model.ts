export interface Institution {
  id: string;

  ancestorDepth?: number;
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
}

export interface InstitutionForm extends Institution {
  adminsEmails: string;
}

/**
 * A factory function that creates Institution
 */
export function createInstitution(params: Partial<Institution>) {
  return {} as Institution;
}
