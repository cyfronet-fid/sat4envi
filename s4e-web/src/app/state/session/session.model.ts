export interface LoginFormState {
  login: string;
  password: string;
  rememberMe: boolean;
  recaptcha: string;
}

export interface Role {
  role: 'INST_ADMIN'|'INST_MANAGER'|'GROUP_MANAGER'|'GROUP_MEMBER',
  institutionSlug: string|null,
  groupSlug: string|null,
}

export interface Session {
  initialized: boolean;
  token: string|null;
  email: string|null;
  roles: Role[];
}

export interface LoginRequestResponse {
  email: string;
  token?: string;
}

/**
 * A factory function that creates Session
 */
export function createSession(params: Partial<Session>) {
  return {
    initialized: false,
    token: params.token || null,
    email: params.email || null,
    roles: params.roles || [],
  } as Session;
}
